package com.ppidemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Migrations in PostgreSQL, listed in {@code db/changelog.txt}.
 *
 * <p>All scripts run in one transaction under an advisory lock, so two
 * nodes starting at once can't both migrate. A script that was already
 * applied must never change: its checksum is verified on every run.</p>
 */
public final class PgMigrations implements Migrations {

    /**
     * Advisory lock key.
     */
    private static final long LOCK = 7_340_001L;

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Classpath directory with scripts and changelog.
     */
    private final String dir;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgMigrations(final DataSource src) {
        this(src, "db");
    }

    /**
     * Ctor.
     * @param src Data source
     * @param path Classpath directory
     */
    public PgMigrations(final DataSource src, final String path) {
        this.source = src;
        this.dir = path;
    }

    @Override
    public void apply() throws IOException {
        try (Connection conn = this.source.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.migrate(conn);
                conn.commit();
            } catch (final SQLException | IOException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (final SQLException ex) {
            throw new IOException("Migration failed", ex);
        }
    }

    /**
     * Apply pending scripts within the given connection.
     * @param conn Connection, not committed yet
     * @throws SQLException If SQL fails
     * @throws IOException If a script is missing or was changed
     */
    private void migrate(final Connection conn)
        throws SQLException, IOException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(String.format("SELECT pg_advisory_xact_lock(%d)", PgMigrations.LOCK));
            stmt.execute(
                String.join(
                    " ",
                    "CREATE TABLE IF NOT EXISTS migration (",
                    "name TEXT PRIMARY KEY, checksum TEXT NOT NULL,",
                    "applied TIMESTAMPTZ NOT NULL DEFAULT now())"
                )
            );
        }
        for (final String name : this.names()) {
            final String script = this.text(name);
            final String sum = this.checksum(script);
            final List<String> known = this.applied(conn, name);
            if (known.isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(script);
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO migration (name, checksum) VALUES (?, ?)"
                )) {
                    stmt.setString(1, name);
                    stmt.setString(2, sum);
                    stmt.executeUpdate();
                }
            } else if (!known.get(0).equals(sum)) {
                throw new IOException(
                    String.format(
                        "Migration %s was changed after it was applied; add a new script instead",
                        name
                    )
                );
            }
        }
    }

    /**
     * Checksum of the script already applied, if any.
     * @param conn Connection
     * @param name Script name
     * @return Empty list or one checksum
     * @throws SQLException If fails
     */
    private List<String> applied(final Connection conn, final String name)
        throws SQLException {
        final List<String> sums = new LinkedList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT checksum FROM migration WHERE name = ?"
        )) {
            stmt.setString(1, name);
            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    sums.add(rset.getString(1));
                }
            }
        }
        return sums;
    }

    /**
     * Script names, in order of application.
     * @return Names
     * @throws IOException If fails
     */
    private List<String> names() throws IOException {
        final List<String> names = new LinkedList<>();
        for (final String line : this.text("changelog.txt").split("\n")) {
            if (!line.isBlank()) {
                names.add(line.trim());
            }
        }
        return names;
    }

    /**
     * Content of a classpath resource in our directory.
     * @param name File name
     * @return Text
     * @throws IOException If not found
     */
    private String text(final String name) throws IOException {
        final String path = String.format("%s/%s", this.dir, name);
        try (InputStream input = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException(String.format("Resource %s not found", path));
            }
            final StringBuilder text = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    text.append(line).append('\n');
                }
            }
            return text.toString();
        }
    }

    /**
     * SHA-256 of a text.
     * @param text Text
     * @return Hex digest
     */
    private String checksum(final String text) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8))
            );
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
