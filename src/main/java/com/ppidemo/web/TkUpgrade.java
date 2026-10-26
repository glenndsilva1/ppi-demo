package com.ppidemo.web;

import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsRedirect;

/**
 * Upgrade a wallet to Full KYC.
 */
public final class TkUpgrade implements TkRegex {

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Ctor.
     * @param wlts Wallets
     */
    public TkUpgrade(final Wallets wlts) {
        this.wallets = wlts;
    }

    @Override
    public Response act(final RqRegex req) throws Exception {
        final Wallet wallet = this.wallets.get(Long.parseLong(req.matcher().group(1)));
        wallet.upgrade();
        return new RsRedirect(String.format("/wallet/%d", wallet.id()));
    }
}
