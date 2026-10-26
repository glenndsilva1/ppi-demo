package com.ppidemo.web;

import com.ppidemo.Accounts;
import com.ppidemo.Agents;
import com.ppidemo.CashOut;
import com.ppidemo.Ledger;
import com.ppidemo.Txn;
import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Cash-out at a BC agent.
 */
public final class TkCashOut implements TkRegex {

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Agents.
     */
    private final Agents agents;

    /**
     * Accounts.
     */
    private final Accounts accounts;

    /**
     * Ledger.
     */
    private final Ledger ledger;

    /**
     * Ctor.
     * @param wlts Wallets
     * @param agts Agents
     * @param accs Accounts
     * @param ldgr Ledger
     */
    public TkCashOut(final Wallets wlts, final Agents agts, final Accounts accs,
        final Ledger ldgr) {
        this.wallets = wlts;
        this.agents = agts;
        this.accounts = accs;
        this.ledger = ldgr;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        final RqFormSmart form = new RqFormSmart(req);
        final Wallet wallet = this.wallets.get(Long.parseLong(req.matcher().group(1)));
        final Txn txn = this.ledger.post(
            new CashOut(
                this.agents.get(Long.parseLong(form.single("agent"))),
                wallet,
                new Paise(form.single("amount")).value(),
                this.accounts.get("COMMISSION_EXPENSE")
            )
        );
        return new RsRedirect(String.format("/wallet/%d?done=%d", wallet.id(), txn.id()));
    }
}
