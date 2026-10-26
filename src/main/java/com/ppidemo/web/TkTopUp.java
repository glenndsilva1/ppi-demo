package com.ppidemo.web;

import com.ppidemo.Accounts;
import com.ppidemo.Agents;
import com.ppidemo.FloatTopUp;
import com.ppidemo.Ledger;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Agent deposits money to raise their float.
 */
public final class TkTopUp implements TkRegex {

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
     * @param agts Agents
     * @param accs Accounts
     * @param ldgr Ledger
     */
    public TkTopUp(final Agents agts, final Accounts accs, final Ledger ldgr) {
        this.agents = agts;
        this.accounts = accs;
        this.ledger = ldgr;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        this.ledger.post(
            new FloatTopUp(
                this.accounts.get("BANK_POOL"),
                this.agents.get(Long.parseLong(req.matcher().group(1))),
                new Paise(new RqFormSmart(req).single("amount")).value()
            )
        );
        return new RsRedirect("/agents");
    }
}
