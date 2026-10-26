package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Accounts in PostgreSQL.
 */
public final class PgAccounts implements Accounts {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgAccounts(final DataSource src) {
        this.source = src;
    }

    @Override
    public Account get(final String code) throws IOException {
        final List<Account> found;
        try {
            found = new JdbcSession(this.source)
                .sql("SELECT id FROM account WHERE code = ?")
                .set(code)
                .select(new ListOutcome<>(rset -> new PgAccount(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Account %s not found", code));
        }
        return found.get(0);
    }

    @Override
    public List<Account> all() throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql("SELECT id FROM account ORDER BY id")
                .select(new ListOutcome<>(rset -> new PgAccount(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
