package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class LedgerTest {

    @Test
    void cashInMovesFloatToWalletAndPaysCommission() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Agent agent = bench.agent(100_000L);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        final Txn txn = bench.cashIn(agent, wallet, 50_000L);
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(50_000L));
        MatcherAssert.assertThat(agent.cash().balance(), Matchers.equalTo(50_000L));
        MatcherAssert.assertThat(agent.commission().balance(), Matchers.equalTo(250L));
        MatcherAssert.assertThat(txn.lines(), Matchers.hasSize(4));
        MatcherAssert.assertThat(txn.amount(), Matchers.equalTo(50_250L));
    }

    @Test
    void oneBalanceServesEveryChannel() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Accounts accounts = new PgAccounts(src);
        final Ledger ledger = new ControlledLedger(src);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        bench.cashIn(bench.agent(100_000L), wallet, 50_000L);
        ledger.post(new CardPayment(wallet, accounts.get("CARD_SETTLEMENT"), "Shop", 20_000L));
        ledger.post(new MetroTap(wallet, accounts.get("TRANSIT_SETTLEMENT"), "Gate", 3_000L));
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(27_000L));
    }

    @Test
    void refusesToSpendMoreThanTheWalletHas() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        bench.cashIn(bench.agent(100_000L), wallet, 10_000L);
        final IllegalArgumentException err = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ControlledLedger(src).post(
                new CardPayment(
                    wallet, new PgAccounts(src).get("CARD_SETTLEMENT"), "Shop", 10_001L
                )
            )
        );
        MatcherAssert.assertThat(
            err.getMessage(),
            Matchers.equalTo(
                "Not enough money: Test Holder's wallet has ₹100.00, this needs ₹100.01"
            )
        );
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(10_000L));
    }

    @Test
    void refusesCashInBeyondTheAgentsFloat() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Agent agent = bench.agent(50_000L);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> bench.cashIn(agent, wallet, 50_001L)
        );
        MatcherAssert.assertThat(agent.cash().balance(), Matchers.equalTo(50_000L));
    }

    @Test
    void capsMinimumKycUntilUpgraded() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Agent agent = bench.agent(5_000_000L);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        final IllegalArgumentException err = Assertions.assertThrows(
            IllegalArgumentException.class, () -> bench.cashIn(agent, wallet, 1_500_000L)
        );
        MatcherAssert.assertThat(err.getMessage(), Matchers.startsWith("Over the limit"));
        wallet.upgrade();
        bench.cashIn(agent, wallet, 1_500_000L);
        MatcherAssert.assertThat(wallet.tier(), Matchers.is(Tier.FULL));
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(1_500_000L));
    }

    @Test
    void freezesSuspendedWallet() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Agent agent = bench.agent(100_000L);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        wallet.suspend();
        final IllegalArgumentException err = Assertions.assertThrows(
            IllegalArgumentException.class, () -> bench.cashIn(agent, wallet, 100L)
        );
        MatcherAssert.assertThat(err.getMessage(), Matchers.containsString("is suspended"));
        wallet.activate();
        bench.cashIn(agent, wallet, 100L);
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(100L));
    }

    @Test
    void databaseEnforcesControlsWithoutJava() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        final Account scheme = new PgAccounts(src).get("CARD_SETTLEMENT");
        final IOException err = Assertions.assertThrows(
            IOException.class,
            () -> new PgLedger(src).post(new CardPayment(wallet, scheme, "Shop", 100L))
        );
        MatcherAssert.assertThat(
            err.getCause().getMessage(), Matchers.containsString("would go below zero")
        );
        wallet.suspend();
        final Agent agent = bench.agent(100_000L);
        final IOException frozen = Assertions.assertThrows(
            IOException.class,
            () -> new PgLedger(src).post(
                new CashIn(agent, wallet, 100L, new PgAccounts(src).get("COMMISSION_EXPENSE"))
            )
        );
        MatcherAssert.assertThat(
            frozen.getCause().getMessage(), Matchers.containsString("is suspended")
        );
    }

    @Test
    void databaseRefusesEditsAndDeletes() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        final Txn txn = bench.cashIn(bench.agent(100_000L), wallet, 700L);
        for (final String sql : List.of(
            "UPDATE line SET amount = 1 WHERE txn = ?",
            "DELETE FROM line WHERE txn = ?",
            "UPDATE txn SET memo = 'hacked' WHERE id = ?",
            "DELETE FROM txn WHERE id = ?"
        )) {
            Assertions.assertThrows(
                SQLException.class,
                () -> new JdbcSession(src).sql(sql).set(txn.id()).execute(),
                sql
            );
        }
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(700L));
    }

    @Test
    void reversesOnceAndOnlyWhileTheMoneyIsThere() throws Exception {
        final DataSource src = new TestSource().value();
        final TestBench bench = new TestBench(src);
        final Ledger ledger = new ControlledLedger(src);
        final Agent agent = bench.agent(100_000L);
        final Wallet wallet = bench.wallet(Tier.MINIMUM);
        final Txn load = bench.cashIn(agent, wallet, 50_000L);
        final Txn pay = ledger.post(
            new CardPayment(wallet, new PgAccounts(src).get("CARD_SETTLEMENT"), "Shop", 40_000L)
        );
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> ledger.post(new Reversal(load))
        );
        final Txn undo = ledger.post(new Reversal(pay));
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(50_000L));
        MatcherAssert.assertThat(undo.reverses(), Matchers.contains(pay.id()));
        MatcherAssert.assertThat(pay.reversedBy(), Matchers.contains(undo.id()));
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> ledger.post(new Reversal(pay))
        );
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> ledger.post(new Reversal(undo))
        );
        final Txn back = ledger.post(new Reversal(load));
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(0L));
        MatcherAssert.assertThat(agent.cash().balance(), Matchers.equalTo(100_000L));
        MatcherAssert.assertThat(agent.commission().balance(), Matchers.equalTo(0L));
        MatcherAssert.assertThat(back.operation(), Matchers.is(Operation.REVERSAL));
    }

    @Test
    void refusesDuplicateMobile() throws Exception {
        final DataSource src = new TestSource().value();
        final String mobile = new TestMobile().toString();
        new PgWallets(src).add("One", mobile, Tier.MINIMUM);
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new PgWallets(src).add("Two", mobile, Tier.FULL)
        );
    }
}
