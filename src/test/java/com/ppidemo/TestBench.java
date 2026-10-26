package com.ppidemo;

import java.io.IOException;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * A fresh agent with a float and a fresh wallet, for one test.
 */
final class TestBench {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    TestBench(final DataSource src) {
        this.source = src;
    }

    /**
     * Agent with a float.
     * @param paise Float, in paise
     * @return Agent
     * @throws IOException If fails
     */
    Agent agent(final long paise) throws IOException {
        final Agent agent = new PgAgents(this.source).add(
            String.format("Shop %s", UUID.randomUUID()), "Testpur"
        );
        new ControlledLedger(this.source).post(
            new FloatTopUp(new PgAccounts(this.source).get("BANK_POOL"), agent, paise)
        );
        return agent;
    }

    /**
     * Empty wallet.
     * @param tier KYC tier
     * @return Wallet
     * @throws IOException If fails
     */
    Wallet wallet(final Tier tier) throws IOException {
        return new PgWallets(this.source).add("Test Holder", new TestMobile().toString(), tier);
    }

    /**
     * Cash-in.
     * @param agent Agent
     * @param wallet Wallet
     * @param paise Amount, in paise
     * @return Transaction
     * @throws IOException If fails
     */
    Txn cashIn(final Agent agent, final Wallet wallet, final long paise) throws IOException {
        return new ControlledLedger(this.source).post(
            new CashIn(
                agent, wallet, paise, new PgAccounts(this.source).get("COMMISSION_EXPENSE")
            )
        );
    }
}
