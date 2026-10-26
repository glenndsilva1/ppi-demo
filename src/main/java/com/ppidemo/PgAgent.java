package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Agent in PostgreSQL.
 */
public final class PgAgent implements Agent {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Number.
     */
    private final long number;

    /**
     * Ctor.
     * @param src Data source
     * @param num Number
     */
    public PgAgent(final DataSource src, final long num) {
        this.source = src;
        this.number = num;
    }

    @Override
    public long id() {
        return this.number;
    }

    @Override
    public String name() throws IOException {
        return this.column("name");
    }

    @Override
    public String place() throws IOException {
        return this.column("place");
    }

    @Override
    public Account cash() throws IOException {
        return new PgAccount(this.source, Long.parseLong(this.column("float_account")));
    }

    @Override
    public Account commission() throws IOException {
        return new PgAccount(this.source, Long.parseLong(this.column("commission_account")));
    }

    /**
     * Read one column of this agent, as text.
     * @param name Column name
     * @return Value
     * @throws IOException If fails
     */
    private String column(final String name) throws IOException {
        final List<String> found;
        try {
            found = new JdbcSession(this.source)
                .sql(String.format("SELECT %s::text FROM agent WHERE id = ?", name))
                .set(this.number)
                .select(new ListOutcome<>(rset -> rset.getString(1)));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Agent #%d not found", this.number));
        }
        return found.get(0);
    }
}
