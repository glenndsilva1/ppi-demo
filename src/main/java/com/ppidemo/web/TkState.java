package com.ppidemo.web;

import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Suspend or activate a wallet.
 */
public final class TkState implements TkRegex {

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Ctor.
     * @param wlts Wallets
     */
    public TkState(final Wallets wlts) {
        this.wallets = wlts;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        final Wallet wallet = this.wallets.get(Long.parseLong(req.matcher().group(1)));
        if ("yes".equals(new RqFormSmart(req).single("active"))) {
            wallet.activate();
        } else {
            wallet.suspend();
        }
        return new RsRedirect(String.format("/wallet/%d", wallet.id()));
    }
}
