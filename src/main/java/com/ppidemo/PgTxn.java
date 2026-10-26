package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import javax.sql.DataSource;

/**
 * Transaction in PostgreSQL.
 */
public final class PgTxn implements Txn {

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
    public PgTxn(final DataSource src, final long num) {
        this.source = src;
        this.number = num;
    }

    @Override
    public long id() {
        return this.number;
    }

    @Override
    public Operation operation() throws IOException {
        return Operation.valueOf(
            this.one("SELECT operation FROM txn WHERE id = ?", rset -> rset.getString(1))
        );
    }

    @Override
    public String memo() throws IOException {
        return this.one("SELECT memo FROM txn WHERE id = ?", rset -> rset.getString(1));
    }

    @Override
    public OffsetDateTime created() throws IOException {
        return this.one(
            "SELECT created FROM txn WHERE id = ?",
            rset -> rset.getObject(1, OffsetDateTime.class)
        );
    }

    @Override
    public List<Line> lines() throws IOException {
        return this.many(
            "SELECT account, side, amount FROM line WHERE txn = ? ORDER BY id",
            rset -> {
                final Account account = new PgAccount(this.source, rset.getLong(1));
                final Line line;
                if ("D".equals(rset.getString(2))) {
                    line = new Debit(account, rset.getLong(3));
                } else {
                    line = new Credit(account, rset.getLong(3));
                }
                return line;
            }
        );
    }

    @Override
    public long amount() throws IOException {
        return this.one(
            "SELECT COALESCE(SUM(amount), 0) FROM line WHERE txn = ? AND side = 'D'",
            rset -> rset.getLong(1)
        );
    }

    @Override
    public List<Long> reverses() throws IOException {
        return this.many(
            "SELECT reverses FROM txn WHERE id = ? AND reverses IS NOT NULL",
            rset -> rset.getLong(1)
        );
    }

    @Override
    public List<Long> reversedBy() throws IOException {
        return this.many("SELECT id FROM txn WHERE reverses = ?", rset -> rset.getLong(1));
    }

    /**
     * Run a query about this transaction that returns one row.
     * @param sql Query with one parameter, the id
     * @param mapping Row to value
     * @param <T> Type of value
     * @return Value
     * @throws IOException If fails or not found
     */
    private <T> T one(final String sql, final ListOutcome.Mapping<T> mapping)
        throws IOException {
        final List<T> found = this.many(sql, mapping);
        if (found.isEmpty()) {
            throw new IOException(String.format("Transaction #%d not found", this.number));
        }
        return found.get(0);
    }

    /**
     * Run a query about this transaction.
     * @param sql Query with one parameter, the id
     * @param mapping Row to value
     * @param <T> Type of value
     * @return Values
     * @throws IOException If fails
     */
    private <T> List<T> many(final String sql, final ListOutcome.Mapping<T> mapping)
        throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql(sql)
                .set(this.number)
                .select(new ListOutcome<>(mapping));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
