package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Transactions in PostgreSQL.
 */
public final class PgTxns implements Txns {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgTxns(final DataSource src) {
        this.source = src;
    }

    @Override
    public Txn get(final long id) throws IOException {
        final List<Txn> found;
        try {
            found = new JdbcSession(this.source)
                .sql("SELECT id FROM txn WHERE id = ?")
                .set(id)
                .select(new ListOutcome<>(rset -> new PgTxn(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Transaction #%d not found", id));
        }
        return found.get(0);
    }

    @Override
    public List<Txn> recent(final int limit) throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql("SELECT id FROM txn ORDER BY id DESC LIMIT ?")
                .set(limit)
                .select(new ListOutcome<>(rset -> new PgTxn(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public List<Txn> touching(final Account account, final int limit) throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql(
                    String.join(
                        " ",
                        "SELECT DISTINCT txn FROM line WHERE account = ?",
                        "ORDER BY txn DESC LIMIT ?"
                    )
                )
                .set(account.id())
                .set(limit)
                .select(new ListOutcome<>(rset -> new PgTxn(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
