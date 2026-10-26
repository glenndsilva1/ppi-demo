package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Wallet in PostgreSQL. Its state lives on its ledger account, so the
 * ledger's controls see it.
 */
public final class PgWallet implements Wallet {

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
    public PgWallet(final DataSource src, final long num) {
        this.source = src;
        this.number = num;
    }

    @Override
    public long id() {
        return this.number;
    }

    @Override
    public String holder() throws IOException {
        return this.column("holder");
    }

    @Override
    public String mobile() throws IOException {
        return this.column("mobile");
    }

    @Override
    public Tier tier() throws IOException {
        return Tier.valueOf(this.column("tier"));
    }

    @Override
    public Account account() throws IOException {
        return new PgAccount(this.source, Long.parseLong(this.column("account")));
    }

    @Override
    public void suspend() throws IOException {
        this.state(false);
    }

    @Override
    public void activate() throws IOException {
        this.state(true);
    }

    @Override
    public void upgrade() throws IOException {
        try {
            new JdbcSession(this.source)
                .autocommit(false)
                .sql("UPDATE wallet SET tier = ? WHERE id = ?")
                .set(Tier.FULL.name())
                .set(this.number)
                .execute()
                .sql("UPDATE account SET cap = ? WHERE id = (SELECT account FROM wallet WHERE id = ?)")
                .set(Tier.FULL.cap())
                .set(this.number)
                .execute()
                .commit();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Switch the account on or off.
     * @param active TRUE to let money move
     * @throws IOException If fails
     */
    private void state(final boolean active) throws IOException {
        try {
            new JdbcSession(this.source)
                .sql("UPDATE account SET active = ? WHERE id = (SELECT account FROM wallet WHERE id = ?)")
                .set(active)
                .set(this.number)
                .execute();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Read one column of this wallet, as text.
     * @param name Column name
     * @return Value
     * @throws IOException If fails
     */
    private String column(final String name) throws IOException {
        final List<String> found;
        try {
            found = new JdbcSession(this.source)
                .sql(String.format("SELECT %s::text FROM wallet WHERE id = ?", name))
                .set(this.number)
                .select(new ListOutcome<>(rset -> rset.getString(1)));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Wallet #%d not found", this.number));
        }
        return found.get(0);
    }
}
