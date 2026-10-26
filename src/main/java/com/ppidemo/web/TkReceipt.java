package com.ppidemo.web;

import com.ppidemo.Account;
import com.ppidemo.Impact;
import com.ppidemo.Line;
import com.ppidemo.Receipt;
import com.ppidemo.Rupees;
import com.ppidemo.Side;
import com.ppidemo.Txn;
import com.ppidemo.Txns;
import java.io.IOException;
import java.util.List;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;

/**
 * Receipt of one transaction, and how the books recorded it.
 */
public final class TkReceipt implements TkRegex {

    /**
     * Transactions.
     */
    private final Txns txns;

    /**
     * Ctor.
     * @param all Transactions
     */
    public TkReceipt(final Txns all) {
        this.txns = all;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Txn txn = this.txns.get(Long.parseLong(req.matcher().group(1)));
        final List<Line> lines = txn.lines();
        final Impact impact = new Impact(lines);
        final StringBuilder html = new StringBuilder(4096);
        html.append("<p class='small'><a href='/books'>← The books</a></p>")
            .append("<section class='card hero'><div>")
            .append(new Chip(txn.operation().channel()))
            .append(String.format("<h1>%s</h1>", txn.operation().title()))
            .append(String.format("<div class='muted'>%s</div>", new Escaped(txn.memo())))
            .append(String.format("<div class='balance'>%s</div>", new Rupees(txn.amount())))
            .append(
                String.format(
                    "<div class='muted small'>Receipt <b>%s</b> · %s</div>",
                    new Receipt(txn.id()), new When(txn.created())
                )
            )
            .append("</div><div>");
        for (final long other : txn.reverses()) {
            html.append(
                String.format(
                    "<div class='notice info'>This undoes <a href='/txn/%d'>%s</a>.</div>",
                    other, new Receipt(other)
                )
            );
        }
        for (final long other : txn.reversedBy()) {
            html.append(
                String.format(
                    "<div class='notice info'>Reversed by <a href='/txn/%d'>%s</a>.</div>",
                    other, new Receipt(other)
                )
            );
        }
        if (txn.reverses().isEmpty() && txn.reversedBy().isEmpty()) {
            html.append(String.format("<form method='post' action='/txn/%d/reverse'>", txn.id()))
                .append("<button class='ghost'>Reverse this transaction</button></form>");
        }
        html.append("</div></section>")
            .append("<h2>Who gained, who gave</h2><div class='grid'>");
        for (final Account account : impact.accounts()) {
            html.append("<div class='card'>")
                .append(String.format("<div class='muted small'>%s</div>", new Escaped(account.title())))
                .append(String.format("<div class='big'>%s</div>", new Change(impact.on(account))))
                .append(
                    String.format(
                        "<div class='muted small'>balance now %s</div>", new Rupees(account.balance())
                    )
                )
                .append("</div>");
        }
        html.append("</div><h2>Journal entry</h2>")
            .append("<div class='wide'><table><tr><th>Account</th><th class='num'>Debit</th>")
            .append("<th class='num'>Credit</th></tr>");
        long debits = 0L;
        long credits = 0L;
        for (final Line line : lines) {
            final String amount = new Rupees(line.amount()).toString();
            if (line.side() == Side.DEBIT) {
                debits += line.amount();
                html.append(
                    String.format(
                        "<tr><td>%s <span class='muted small'>%s</span></td><td class='num'>%s</td><td></td></tr>",
                        new Escaped(line.account().title()), new Escaped(line.account().code()), amount
                    )
                );
            } else {
                credits += line.amount();
                html.append(
                    String.format(
                        "<tr><td>%s <span class='muted small'>%s</span></td><td></td><td class='num'>%s</td></tr>",
                        new Escaped(line.account().title()), new Escaped(line.account().code()), amount
                    )
                );
            }
        }
        html.append(
            String.format(
                "<tr><td><b>Total</b></td><td class='num'><b>%s</b></td><td class='num'><b>%s</b></td></tr>",
                new Rupees(debits), new Rupees(credits)
            )
        ).append("</table></div>")
            .append("<p class='muted small'>Debits always equal credits. The database refuses any ")
            .append("other entry, and refuses to edit or delete this one: a mistake is fixed by a ")
            .append("reversal, which leaves both entries in the books.</p>");
        return new RsPage(new Receipt(txn.id()).toString(), html);
    }
}
