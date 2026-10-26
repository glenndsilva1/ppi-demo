package com.ppidemo.web;

import com.ppidemo.Tier;
import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Open a wallet.
 */
public final class TkWalletAdd implements Take {

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Ctor.
     * @param wlts Wallets
     */
    public TkWalletAdd(final Wallets wlts) {
        this.wallets = wlts;
    }

    @Override
    public Response act(final Request req) throws Exception {
        final RqFormSmart form = new RqFormSmart(req);
        final Wallet wallet = this.wallets.add(
            form.single("holder"),
            form.single("mobile").replaceAll("[^0-9]", ""),
            Tier.valueOf(form.single("tier"))
        );
        return new RsRedirect(String.format("/wallet/%d", wallet.id()));
    }
}
