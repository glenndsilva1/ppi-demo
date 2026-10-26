package com.ppidemo.web;

import com.ppidemo.Account;
import com.ppidemo.Impact;
import com.ppidemo.Receipt;
import com.ppidemo.Txn;
import java.io.IOException;
import java.util.List;

/**
 * Transactions as an HTML table, each with its effect on one account.
 */
public final class ActivityTable {

    /**
     * Transactions, most recent first.
     */
    private final List<Txn> txns;

    /**
     * Account whose balance changes are shown.
     */
    private final Account account;

    /**
     * Page to come back to after a reversal.
     */
    private final String back;

    /**
     * Ctor.
     * @param all Transactions
     * @param acc Account whose balance changes are shown
     * @param path Page to come back to after a reversal
     */
    public ActivityTable(final List<Txn> all, final Account acc, final String path) {
        this.txns = all;
        this.account = acc;
        this.back = path;
    }

    @Override
    public String toString() {
        try {
            return this.html();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Render.
     * @return HTML
     * @throws IOException If fails
     */
    private String html() throws IOException {
        if (this.txns.isEmpty()) {
            return "<div class='card muted'>Nothing yet. Try a cash-in above.</div>";
        }
        final StringBuilder html = new StringBuilder(4096);
        html.append("<div class='wide'><table><tr><th>When</th><th>What</th><th>Channel</th>")
            .append("<th class='num'>Amount</th><th>Receipt</th><th></th></tr>");
        for (final Txn txn : this.txns) {
            final boolean undone = !txn.reversedBy().isEmpty();
            String what = String.format(
                "<b>%s</b><div class='muted small'>%s</div>",
                txn.operation().title(), new Escaped(txn.memo())
            );
            if (undone) {
                what = String.format(
                    "<div class='struck'>%s</div><div class='small'>Reversed by %s</div>",
                    what, new Receipt(txn.reversedBy().get(0))
                );
            }
            html.append("<tr>")
                .append(String.format("<td class='small muted'>%s</td>", new When(txn.created())))
                .append(String.format("<td>%s</td>", what))
                .append(String.format("<td>%s</td>", new Chip(txn.operation().channel())))
                .append(
                    String.format(
                        "<td class='num'>%s</td>",
                        new Change(new Impact(txn.lines()).on(this.account))
                    )
                )
                .append(
                    String.format(
                        "<td><a href='/txn/%d'>%s</a></td>", txn.id(), new Receipt(txn.id())
                    )
                )
                .append("<td>");
            if (!undone && txn.reverses().isEmpty()) {
                html.append(
                    String.format(
                        String.join(
                            "",
                            "<form method='post' action='/txn/%d/reverse'>",
                            "<input type='hidden' name='back' value='%s'>",
                            "<button class='link'>Reverse</button></form>"
                        ),
                        txn.id(), new Escaped(this.back)
                    )
                );
            }
            html.append("</td></tr>");
        }
        return html.append("</table></div>").toString();
    }
}
