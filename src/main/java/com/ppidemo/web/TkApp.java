package com.ppidemo.web;

import com.ppidemo.Accounts;
import com.ppidemo.Agents;
import com.ppidemo.ControlledLedger;
import com.ppidemo.Demo;
import com.ppidemo.Ledger;
import com.ppidemo.PgAccounts;
import com.ppidemo.PgAgents;
import com.ppidemo.PgTxns;
import com.ppidemo.PgWallets;
import com.ppidemo.Txns;
import com.ppidemo.Wallets;
import javax.sql.DataSource;
import org.takes.facets.fallback.FbChain;
import org.takes.facets.fallback.FbStatus;
import org.takes.facets.fallback.TkFallback;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;
import org.takes.rs.RsText;
import org.takes.rs.RsWithStatus;
import org.takes.tk.TkWrap;

/**
 * The whole web application: routes and error pages.
 */
public final class TkApp extends TkWrap {

    /**
     * Wallet in a URL.
     */
    private static final String WALLET = "/wallet/([0-9]+)";

    /**
     * Ctor.
     * @param src Data source
     */
    public TkApp(final DataSource src) {
        this(
            src, new PgWallets(src), new PgAgents(src), new PgAccounts(src),
            new PgTxns(src), new ControlledLedger(src)
        );
    }

    /**
     * Ctor.
     * @param src Data source
     * @param wallets Wallets
     * @param agents Agents
     * @param accounts Accounts
     * @param txns Transactions
     * @param ledger Ledger
     */
    private TkApp(final DataSource src, final Wallets wallets, final Agents agents,
        final Accounts accounts, final Txns txns, final Ledger ledger) {
        super(
            new TkFallback(
                new TkFork(
                    new FkMethods(
                        "GET",
                        new TkFork(
                            new FkRegex("/", new TkHome(wallets)),
                            new FkRegex(TkApp.WALLET, new TkWallet(wallets, agents, txns)),
                            new FkRegex("/agents", new TkAgents(agents)),
                            new FkRegex("/books", new TkBooks(accounts, txns)),
                            new FkRegex("/txn/([0-9]+)", new TkReceipt(txns))
                        )
                    ),
                    new FkMethods(
                        "POST",
                        new TkFork(
                            new FkRegex("/wallets", new TkWalletAdd(wallets)),
                            new FkRegex(
                                String.format("%s/cash-in", TkApp.WALLET),
                                new TkCashIn(wallets, agents, accounts, ledger)
                            ),
                            new FkRegex(
                                String.format("%s/cash-out", TkApp.WALLET),
                                new TkCashOut(wallets, agents, accounts, ledger)
                            ),
                            new FkRegex(
                                String.format("%s/card", TkApp.WALLET),
                                new TkCard(wallets, accounts, ledger)
                            ),
                            new FkRegex(
                                String.format("%s/tap", TkApp.WALLET),
                                new TkTap(wallets, accounts, ledger)
                            ),
                            new FkRegex(String.format("%s/state", TkApp.WALLET), new TkState(wallets)),
                            new FkRegex(
                                String.format("%s/upgrade", TkApp.WALLET), new TkUpgrade(wallets)
                            ),
                            new FkRegex("/txn/([0-9]+)/reverse", new TkReverse(txns, ledger)),
                            new FkRegex("/agents", new TkAgentAdd(agents)),
                            new FkRegex(
                                "/agents/([0-9]+)/float", new TkTopUp(agents, accounts, ledger)
                            ),
                            new FkRegex("/reset", new TkReset(new Demo(src)))
                        )
                    )
                ),
                new FbChain(
                    new FbStatus(404, new RsWithStatus(new RsText("Page not found"), 404)),
                    new FbRefusal(),
                    req -> {
                        req.throwable().printStackTrace();
                        return new Opt.Single<>(
                            new RsWithStatus(
                                new RsText(
                                    String.format("Internal error: %s", req.throwable().getMessage())
                                ),
                                500
                            )
                        );
                    }
                )
            )
        );
    }
}
