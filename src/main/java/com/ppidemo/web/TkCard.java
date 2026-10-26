package com.ppidemo.web;

import com.ppidemo.Accounts;
import com.ppidemo.CardPayment;
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
 * Prepaid card payment at a shop.
 */
public final class TkCard implements TkRegex {

    /**
     * Wallets.
     */
    private final Wallets wallets;

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
     * @param accs Accounts
     * @param ldgr Ledger
     */
    public TkCard(final Wallets wlts, final Accounts accs, final Ledger ldgr) {
        this.wallets = wlts;
        this.accounts = accs;
        this.ledger = ldgr;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        final RqFormSmart form = new RqFormSmart(req);
        final Wallet wallet = this.wallets.get(Long.parseLong(req.matcher().group(1)));
        final Txn txn = this.ledger.post(
            new CardPayment(
                wallet,
                this.accounts.get("CARD_SETTLEMENT"),
                form.single("merchant", "a shop"),
                new Paise(form.single("amount")).value()
            )
        );
        return new RsRedirect(String.format("/wallet/%d?done=%d", wallet.id(), txn.id()));
    }
}
