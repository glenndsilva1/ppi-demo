package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Demo data: two agents and two wallets, one with a little history.
 *
 * <p>Reset wipes the whole schema, journal included, which nothing else
 * in this system may do. Never point a real database at it.</p>
 */
public final class Demo {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source, migrated
     */
    public Demo(final DataSource src) {
        this.source = src;
    }

    /**
     * Put demo data in, unless there are wallets already.
     * @throws IOException If fails
     */
    public void fill() throws IOException {
        if (new PgWallets(this.source).all().isEmpty()) {
            this.seed();
        }
    }

    /**
     * Throw everything away and start over.
     * @throws IOException If fails
     */
    public void reset() throws IOException {
        try {
            new JdbcSession(this.source)
                .sql("DROP SCHEMA public CASCADE")
                .execute()
                .sql("CREATE SCHEMA public")
                .execute();
        } catch (final SQLException ex) {
            throw new IOException("Can't reset the demo", ex);
        }
        new PgMigrations(this.source).apply();
        this.seed();
    }

    /**
     * Create the demo agents, wallets and history.
     * @throws IOException If fails
     */
    private void seed() throws IOException {
        final Accounts accounts = new PgAccounts(this.source);
        final Account bank = accounts.get("BANK_POOL");
        final Account expense = accounts.get("COMMISSION_EXPENSE");
        final Ledger ledger = new ControlledLedger(this.source);
        final Agents agents = new PgAgents(this.source);
        final Agent ravi = agents.add("Ravi Kirana Store", "Nashik");
        ledger.post(new FloatTopUp(bank, ravi, 2_000_000L));
        final Agent sunita = agents.add("Sunita CSC Centre", "Pune");
        ledger.post(new FloatTopUp(bank, sunita, 50_000L));
        final Wallets wallets = new PgWallets(this.source);
        wallets.add("Asha Patil", "9876543210", Tier.MINIMUM);
        final Wallet rahul = wallets.add("Rahul Mehta", "9123456780", Tier.FULL);
        ledger.post(new CashIn(ravi, rahul, 200_000L, expense));
        ledger.post(
            new CardPayment(
                rahul, accounts.get("CARD_SETTLEMENT"), "Reliance Smart, Nashik", 34_900L
            )
        );
        ledger.post(
            new MetroTap(
                rahul, accounts.get("TRANSIT_SETTLEMENT"), "Pune Metro, Shivajinagar", 3_000L
            )
        );
    }
}
