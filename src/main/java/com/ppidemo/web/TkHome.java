package com.ppidemo.web;

import com.ppidemo.Account;
import com.ppidemo.Rupees;
import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

/**
 * Home page: the channels, the wallets, and the demo script.
 */
public final class TkHome implements Take {

    /**
     * Channels on the platform: name, what it does, roadmap phase, live in demo.
     */
    private static final String[][] CHANNELS = {
        {"BC wallet", "Cash-in and cash-out at agent shops", "Phase 1", "yes"},
        {"NCMC transit", "Tap at metro and bus gates", "Phase 1", "yes"},
        {"PPI card", "Virtual or physical prepaid card", "Phase 2", "yes"},
        {"Wallet app", "Fund, pay, transfer, view balance", "Phase 2", "no"},
        {"UPI & QR", "VPA and QR payments", "Phase 3", "no"},
        {"NETC FASTag", "Tolls, parking and fleet", "Phase 3", "no"},
    };

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Ctor.
     * @param wlts Wallets
     */
    public TkHome(final Wallets wlts) {
        this.wallets = wlts;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final StringBuilder html = new StringBuilder(4096);
        html.append("<h1>One wallet. Every channel.</h1>")
            .append("<p class='lead'>A cash-in at a BC agent, a card payment and a metro tap ")
            .append("all use the <b>same wallet balance</b>, under the same limits and KYC rules. ")
            .append("Every rupee is written to one ledger that can never be edited.</p>")
            .append("<div class='grid'>");
        for (final String[] channel : TkHome.CHANNELS) {
            final boolean live = "yes".equals(channel[3]);
            html.append(String.format("<div class='card tile%s'>", live ? "" : " off"))
                .append(new Chip(channel[0]))
                .append(String.format("<h3>%s</h3>", new Escaped(channel[0])))
                .append(String.format("<div class='muted small'>%s</div>", new Escaped(channel[1])))
                .append("<div>");
            if (live) {
                html.append("<span class='tag ok'>Try it in this demo</span>");
            } else {
                html.append("<span class='tag soon'>Coming</span>");
            }
            html.append(String.format("<span class='tag soon'>%s</span>", channel[2]))
                .append("</div></div>");
        }
        html.append("</div><div class='split'><section><h2>Customer wallets</h2><div class='grid'>");
        for (final Wallet wallet : this.wallets.all()) {
            final Account account = wallet.account();
            html.append("<div class='card'>")
                .append(
                    String.format(
                        "<h3>%s</h3><div class='muted small'>%s</div>",
                        new Escaped(wallet.holder()), new Escaped(wallet.mobile())
                    )
                )
                .append(String.format("<div class='big'>%s</div>", new Rupees(account.balance())))
                .append(String.format("<span class='tag'>%s</span>", wallet.tier().title()));
            if (account.active()) {
                html.append("<span class='tag ok'>Active</span>");
            } else {
                html.append("<span class='tag bad'>Suspended</span>");
            }
            html.append(
                String.format(
                    "<p style='margin:12px 0 0'><a href='/wallet/%d'><b>Open wallet →</b></a></p>",
                    wallet.id()
                )
            ).append("</div>");
        }
        html.append("</div>")
            .append("<h2>Open a new wallet</h2>")
            .append("<form class='card' method='post' action='/wallets'>")
            .append("<label>Customer name<input name='holder' required placeholder='e.g. Priya Shah'></label>")
            .append("<label>Mobile<input name='mobile' required inputmode='numeric' maxlength='10' placeholder='10 digits'></label>")
            .append("<label>KYC<select name='tier'>")
            .append("<option value='MINIMUM'>Minimum KYC (holds up to ₹10,000)</option>")
            .append("<option value='FULL'>Full KYC (holds up to ₹2,00,000)</option>")
            .append("</select></label><button>Open wallet</button></form></section>")
            .append("<aside><h2>Demo script</h2><div class='card'><ol class='steps'>")
            .append("<li>Open <b>Asha Patil</b>'s wallet. It is empty, Minimum KYC, limit ₹10,000.</li>")
            .append("<li><b>Cash-in ₹500</b> at Ravi Kirana Store. Ravi's float goes down, he earns ₹2.50.</li>")
            .append("<li><b>Pay ₹200 by card</b>, then <b>tap ₹30 at the metro</b>. Same balance: ₹270 left.</li>")
            .append("<li>Try a <b>₹1,000 card payment</b>: refused, not enough money.</li>")
            .append("<li>Try a <b>₹15,000 cash-in</b>: refused, over the Minimum KYC limit. ")
            .append("<b>Upgrade to Full KYC</b> and try again.</li>")
            .append("<li><b>Suspend</b> the wallet and tap at the metro: refused.</li>")
            .append("<li>Activate it and <b>reverse</b> the card payment: the money comes back, ")
            .append("on a new receipt linked to the old one.</li>")
            .append("<li>Cash-in ₹1,000 at <b>Sunita CSC Centre</b>: refused, her float is only ₹500. ")
            .append("Top it up on <a href='/agents'>BC agents</a>.</li>")
            .append("<li>Open <a href='/books'>The books</a>: every rupee is accounted for, ")
            .append("debits equal credits.</li>")
            .append("</ol></div>")
            .append("<form method='post' action='/reset'>")
            .append("<button class='danger'>Reset demo to the start</button>")
            .append("<div class='muted small'>Wipes everything and loads the demo data again.</div>")
            .append("</form></aside></div>");
        return new RsPage("Wallets", html);
    }
}
