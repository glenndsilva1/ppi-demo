package com.ppidemo.web;

import com.ppidemo.Account;
import com.ppidemo.Accounts;
import com.ppidemo.Kind;
import com.ppidemo.Receipt;
import com.ppidemo.Rupees;
import com.ppidemo.Txn;
import com.ppidemo.Txns;
import java.io.IOException;
import java.util.List;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

/**
 * The books: every account, proof that they balance, and the journal.
 */
public final class TkBooks implements Take {

    /**
     * Accounts.
     */
    private final Accounts accounts;

    /**
     * Transactions.
     */
    private final Txns txns;

    /**
     * Ctor.
     * @param accs Accounts
     * @param all Transactions
     */
    public TkBooks(final Accounts accs, final Txns all) {
        this.accounts = accs;
        this.txns = all;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final List<Account> all = this.accounts.all();
        long held = 0L;
        long owed = 0L;
        long customers = 0L;
        long spent = 0L;
        for (final Account account : all) {
            final Kind kind = account.kind();
            final long balance = account.balance();
            if (kind == Kind.ASSET) {
                held += balance;
            } else if (kind == Kind.EXPENSE) {
                spent += balance;
            } else {
                owed += balance;
            }
            if (account.code().startsWith("WALLET-")) {
                customers += balance;
            }
        }
        final StringBuilder html = new StringBuilder(8192);
        html.append("<h1>The books</h1>")
            .append("<p class='lead'>One ledger behind every channel. Each account's balance is ")
            .append("the sum of its journal lines, so it can always be explained, line by line.</p>")
            .append("<div class='grid'>")
            .append(this.stat("Held at the sponsor bank", held))
            .append(this.stat("Customer money in wallets", customers))
            .append(this.stat("Owed to customers, agents and schemes", owed))
            .append(this.stat("Commission paid to agents", spent))
            .append("</div>");
        if (held + spent == owed) {
            html.append(
                String.format(
                    "<div class='notice ok'>✓ <b>The books balance.</b> Bank %s + commission paid %s = owed %s.</div>",
                    new Rupees(held), new Rupees(spent), new Rupees(owed)
                )
            );
        } else {
            html.append(
                String.format(
                    "<div class='notice bad'>The books are off by %s.</div>",
                    new Rupees(held + spent - owed)
                )
            );
        }
        html.append("<h2>Accounts</h2><div class='wide'><table>")
            .append("<tr><th>Account</th><th>Code</th><th>Kind</th><th class='num'>Balance</th></tr>");
        for (final Account account : all) {
            html.append(
                String.format(
                    "<tr><td>%s</td><td class='muted small'>%s</td><td class='small'>%s</td><td class='num'>%s</td></tr>",
                    new Escaped(account.title()), new Escaped(account.code()),
                    account.kind().name().toLowerCase(), new Rupees(account.balance())
                )
            );
        }
        html.append("</table></div><h2>Journal</h2><div class='wide'><table>")
            .append("<tr><th>Receipt</th><th>When</th><th>What</th><th>Channel</th><th class='num'>Amount</th></tr>");
        for (final Txn txn : this.txns.recent(100)) {
            html.append(
                String.format(
                    "<tr><td><a href='/txn/%d'>%s</a></td><td class='small muted'>%s</td><td><b>%s</b><div class='muted small'>%s</div></td><td>%s</td><td class='num'>%s</td></tr>",
                    txn.id(), new Receipt(txn.id()), new When(txn.created()),
                    txn.operation().title(), new Escaped(txn.memo()),
                    new Chip(txn.operation().channel()), new Rupees(txn.amount())
                )
            );
        }
        html.append("</table></div>");
        return new RsPage("The books", html);
    }

    /**
     * Headline figure.
     * @param title What it is
     * @param paise Amount, in paise
     * @return HTML
     */
    private String stat(final String title, final long paise) {
        return String.format(
            "<div class='card'><div class='muted small'>%s</div><div class='big'>%s</div></div>",
            title, new Rupees(paise)
        );
    }
}
