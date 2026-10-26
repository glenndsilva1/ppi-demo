package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import com.jcabi.jdbc.SingleOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Account in PostgreSQL. Holds no data, only asks the database.
 */
public final class PgAccount implements Account {

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
    public PgAccount(final DataSource src, final long num) {
        this.source = src;
        this.number = num;
    }

    @Override
    public long id() {
        return this.number;
    }

    @Override
    public String code() throws IOException {
        return this.column("code", String.class);
    }

    @Override
    public String title() throws IOException {
        return this.column("title", String.class);
    }

    @Override
    public Kind kind() throws IOException {
        return Kind.valueOf(this.column("kind", String.class));
    }

    @Override
    public long balance() throws IOException {
        final List<Long> found;
        try {
            found = new JdbcSession(this.source)
                .sql(
                    String.join(
                        " ",
                        "SELECT a.kind,",
                        "COALESCE(SUM(l.amount) FILTER (WHERE l.side = 'D'), 0),",
                        "COALESCE(SUM(l.amount) FILTER (WHERE l.side = 'C'), 0)",
                        "FROM account a LEFT JOIN line l ON l.account = a.id",
                        "WHERE a.id = ? GROUP BY a.kind"
                    )
                )
                .set(this.number)
                .select(
                    new ListOutcome<>(
                        rset -> Kind.valueOf(rset.getString(1))
                            .balance(rset.getLong(2), rset.getLong(3))
                    )
                );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Account #%d not found", this.number));
        }
        return found.get(0);
    }

    @Override
    public long cap() throws IOException {
        return this.column("cap", Long.class);
    }

    @Override
    public boolean active() throws IOException {
        return this.column("active", Boolean.class);
    }

    /**
     * Read one column of this account.
     * @param name Column name
     * @param type Java type of the value
     * @param <T> Type of the value
     * @return Value
     * @throws IOException If fails
     */
    private <T> T column(final String name, final Class<T> type) throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql(String.format("SELECT %s FROM account WHERE id = ?", name))
                .set(this.number)
                .select(new SingleOutcome<>(type));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
