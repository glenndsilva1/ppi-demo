package com.ppidemo.web;

import com.ppidemo.Ledger;
import com.ppidemo.Reversal;
import com.ppidemo.Txn;
import com.ppidemo.Txns;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Reverse a transaction, then go back to the wallet it came from, or
 * to the new receipt.
 */
public final class TkReverse implements TkRegex {

    /**
     * Transactions.
     */
    private final Txns txns;

    /**
     * Ledger.
     */
    private final Ledger ledger;

    /**
     * Ctor.
     * @param all Transactions
     * @param ldgr Ledger
     */
    public TkReverse(final Txns all, final Ledger ldgr) {
        this.txns = all;
        this.ledger = ldgr;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        final Txn txn = this.ledger.post(
            new Reversal(this.txns.get(Long.parseLong(req.matcher().group(1))))
        );
        final String back = new RqFormSmart(req).single("back", "");
        final String target;
        if (back.matches("/wallet/[0-9]+")) {
            target = String.format("%s?done=%d", back, txn.id());
        } else {
            target = String.format("/txn/%d", txn.id());
        }
        return new RsRedirect(target);
    }
}
