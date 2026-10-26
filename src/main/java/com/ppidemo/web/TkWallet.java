package com.ppidemo.web;

import com.ppidemo.Account;
import com.ppidemo.Agent;
import com.ppidemo.Agents;
import com.ppidemo.Impact;
import com.ppidemo.Operation;
import com.ppidemo.Receipt;
import com.ppidemo.Rupees;
import com.ppidemo.Tier;
import com.ppidemo.Txn;
import com.ppidemo.Txns;
import com.ppidemo.Wallet;
import com.ppidemo.Wallets;
import java.io.IOException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;

/**
 * One wallet: its balance, its controls, every channel that can use it,
 * and what happened to it.
 */
public final class TkWallet implements TkRegex {

    /**
     * Metro stations to tap at.
     */
    private static final String[] STATIONS = {
        "Pune Metro, Shivajinagar",
        "Pune Metro, Swargate",
        "Mumbai Metro, Andheri",
        "Mumbai Metro, Ghatkopar",
    };

    /**
     * Wallets.
     */
    private final Wallets wallets;

    /**
     * Agents.
     */
    private final Agents agents;

    /**
     * Transactions.
     */
    private final Txns txns;

    /**
     * Ctor.
     * @param wlts Wallets
     * @param agts Agents
     * @param all Transactions
     */
    public TkWallet(final Wallets wlts, final Agents agts, final Txns all) {
        this.wallets = wlts;
        this.agents = agts;
        this.txns = all;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Wallet wallet = this.wallets.get(Long.parseLong(req.matcher().group(1)));
        final Account account = wallet.account();
        final long balance = account.balance();
        final long cap = account.cap();
        final String path = String.format("/wallet/%d", wallet.id());
        final StringBuilder html = new StringBuilder(8192);
        html.append("<p class='small'><a href='/'>← All wallets</a></p>");
        final String done = new RqHref.Smart(req).single("done", "");
        if (done.matches("[0-9]+")) {
            final Txn txn = this.txns.get(Long.parseLong(done));
            html.append("<div class='notice ok'>✓ <b>Done.</b> ")
                .append(
                    String.format(
                        "%s %s, receipt <a href='/txn/%d'>%s</a>",
                        txn.operation().title(), new Change(new Impact(txn.lines()).on(account)),
                        txn.id(), new Receipt(txn.id())
                    )
                )
                .append("<div class='small' style='margin-top:6px'>Balances that changed:</div><ul class='small' style='margin:4px 0 0'>");
            final Impact impact = new Impact(txn.lines());
            for (final Account touched : impact.accounts()) {
                html.append(
                    String.format(
                        "<li>%s: %s, now <b>%s</b></li>",
                        new Escaped(touched.title()), new Change(impact.on(touched)),
                        new Rupees(touched.balance())
                    )
                );
            }
            html.append("</ul>")
                .append("</div>");
        }
        html.append("<section class='card hero'><div>")
            .append(
                String.format(
                    "<div class='muted'>%s · %s</div>",
                    new Escaped(wallet.holder()), new Escaped(wallet.mobile())
                )
            )
            .append(String.format("<div class='balance'>%s</div>", new Rupees(balance)))
            .append(String.format("<span class='tag'>%s</span>", wallet.tier().title()));
        if (account.active()) {
            html.append("<span class='tag ok'>Active</span>");
        } else {
            html.append("<span class='tag bad'>Suspended</span>");
        }
        html.append(
            String.format(
                "<div class='meter'><span style='width:%d%%'></span></div>",
                Math.min(100L, balance * 100L / Math.max(cap, 1L))
            )
        ).append(
            String.format(
                "<div class='muted small'>%s of the %s limit for %s</div>",
                new Rupees(balance), new Rupees(cap), wallet.tier().title()
            )
        ).append("</div><div>");
        if (account.active()) {
            html.append(String.format("<form method='post' action='%s/state'>", path))
                .append("<input type='hidden' name='active' value='no'>")
                .append("<button class='danger'>Suspend wallet</button></form>");
        } else {
            html.append(String.format("<form method='post' action='%s/state'>", path))
                .append("<input type='hidden' name='active' value='yes'>")
                .append("<button>Activate wallet</button></form>");
        }
        if (wallet.tier() == Tier.MINIMUM) {
            html.append(String.format("<form method='post' action='%s/upgrade'>", path))
                .append(
                    String.format(
                        "<button class='ghost'>Upgrade to Full KYC (limit %s)</button></form>",
                        new Rupees(Tier.FULL.cap())
                    )
                );
        }
        html.append("</div></section>")
            .append("<h2>Use the same balance on any channel</h2><div class='grid'>")
            .append(this.cash(path, "cash-in", "Cash-in at agent", "Customer hands cash to the agent; the wallet goes up.", "500"))
            .append(this.cash(path, "cash-out", "Cash-out at agent", "Agent hands cash to the customer; the wallet goes down.", "100"))
            .append(String.format("<form class='card' method='post' action='%s/card'>", path))
            .append(new Chip(Operation.CARD.channel()))
            .append("<h3>Pay at a shop</h3><div class='muted small'>Prepaid card, tapped or swiped.</div>")
            .append("<label>Shop<input name='merchant' value='Big Bazaar, Nashik' required></label>")
            .append("<label>Amount (₹)<input name='amount' value='200' inputmode='decimal' required></label>")
            .append("<button>Pay by card</button></form>")
            .append(String.format("<form class='card' method='post' action='%s/tap'>", path))
            .append(new Chip(Operation.TRANSIT.channel()))
            .append("<h3>Tap at the metro</h3><div class='muted small'>The gate takes the fare.</div>")
            .append("<label>Station<select name='station'>");
        for (final String station : TkWallet.STATIONS) {
            html.append(String.format("<option>%s</option>", new Escaped(station)));
        }
        html.append("</select></label><label>Fare</label><div class='fares'>")
            .append("<button name='fare' value='20'>₹20</button>")
            .append("<button name='fare' value='30'>₹30</button>")
            .append("<button name='fare' value='50'>₹50</button>")
            .append("</div></form></div>")
            .append("<h2>Activity</h2>");
        html.append(new ActivityTable(this.txns.touching(account, 50), account, path));
        return new RsPage(wallet.holder(), html);
    }

    /**
     * Form for a cash service at an agent.
     * @param path Path of the wallet
     * @param action Last part of the form's path
     * @param title Title
     * @param hint What happens
     * @param amount Amount filled in, in rupees
     * @return HTML
     * @throws IOException If fails
     */
    private String cash(final String path, final String action, final String title,
        final String hint, final String amount) throws IOException {
        final StringBuilder html = new StringBuilder(1024);
        html.append(String.format("<form class='card' method='post' action='%s/%s'>", path, action))
            .append(new Chip(Operation.CASH_IN.channel()))
            .append(String.format("<h3>%s</h3><div class='muted small'>%s</div>", title, hint))
            .append("<label>Agent<select name='agent'>");
        for (final Agent agent : this.agents.all()) {
            html.append(
                String.format(
                    "<option value='%d'>%s, %s</option>",
                    agent.id(), new Escaped(agent.name()), new Escaped(agent.place())
                )
            );
        }
        return html.append("</select></label>")
            .append(
                String.format(
                    "<label>Amount (₹)<input name='amount' value='%s' inputmode='decimal' required></label>",
                    amount
                )
            )
            .append(String.format("<button>%s</button></form>", title.replace(" at agent", "")))
            .toString();
    }
}
