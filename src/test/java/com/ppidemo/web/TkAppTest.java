package com.ppidemo.web;

import com.ppidemo.PgAgents;
import com.ppidemo.PgWallets;
import com.ppidemo.TestMobile;
import com.ppidemo.TestSource;
import com.ppidemo.Tier;
import com.ppidemo.Wallet;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rs.RsBodyPrint;
import org.takes.rs.RsPrint;

/**
 * The demo, through HTTP, as a presenter would click it.
 */
final class TkAppTest {

    @Test
    void walksTheDemoStory() throws Exception {
        final DataSource src = new TestSource().value();
        final Take app = new TkApp(src);
        final String agent = Long.toString(new PgAgents(src).add("Web <Shop>", "Pune").id());
        this.post(app, String.format("/agents/%s/float", agent), "amount", "1000");
        final Wallet wallet = new PgWallets(src).add(
            "Asha <b>Patil</b>", new TestMobile().toString(), Tier.MINIMUM
        );
        final String path = String.format("/wallet/%d", wallet.id());
        this.post(app, path + "/cash-in", "agent", agent, "amount", "500");
        this.post(app, path + "/card", "merchant", "Big Bazaar", "amount", "200");
        this.post(app, path + "/tap", "station", "Swargate", "fare", "30");
        final String html = new RsBodyPrint(app.act(new RqFake("GET", path))).asString();
        MatcherAssert.assertThat(
            html,
            Matchers.allOf(
                Matchers.containsString("₹270.00"),
                Matchers.containsString("Asha &lt;b&gt;Patil&lt;/b&gt;"),
                Matchers.not(Matchers.containsString("<b>Patil</b>")),
                Matchers.containsString("Cash-in at agent"),
                Matchers.containsString("Card payment"),
                Matchers.containsString("Metro tap")
            )
        );
        MatcherAssert.assertThat(
            new RsPrint(app.act(this.form(path + "/card", "merchant", "X", "amount", "1000"))).print(),
            Matchers.allOf(
                Matchers.startsWith("HTTP/1.1 400"),
                Matchers.containsString("Not enough money")
            )
        );
        final String suspended = new RsPrint(
            app.act(this.form(path + "/state", "active", "no"))
        ).print();
        MatcherAssert.assertThat(suspended, Matchers.startsWith("HTTP/1.1 303"));
        MatcherAssert.assertThat(
            new RsPrint(app.act(this.form(path + "/tap", "station", "S", "fare", "20"))).print(),
            Matchers.containsString("is suspended")
        );
        this.post(app, path + "/state", "active", "yes");
        for (final String page : new String[] {"/", "/agents", "/books", path}) {
            MatcherAssert.assertThat(
                page,
                new RsPrint(app.act(new RqFake("GET", page))).print(),
                Matchers.startsWith("HTTP/1.1 200")
            );
        }
    }

    @Test
    void reversesFromTheWalletPage() throws Exception {
        final DataSource src = new TestSource().value();
        final Take app = new TkApp(src);
        final String agent = Long.toString(new PgAgents(src).add("Shop", "Pune").id());
        this.post(app, String.format("/agents/%s/float", agent), "amount", "1000");
        final Wallet wallet = new PgWallets(src).add(
            "Rev Tester", new TestMobile().toString(), Tier.FULL
        );
        final String path = String.format("/wallet/%d", wallet.id());
        this.post(app, path + "/cash-in", "agent", agent, "amount", "100");
        final String location = new RsPrint(
            app.act(this.form(path + "/card", "merchant", "Shop", "amount", "40"))
        ).print().replaceAll("(?s).*Location: [^\\n]*done=([0-9]+).*", "$1");
        MatcherAssert.assertThat(
            new RsPrint(
                app.act(this.form(String.format("/txn/%s/reverse", location), "back", path))
            ).print(),
            Matchers.containsString(String.format("Location: %s?done=", path))
        );
        MatcherAssert.assertThat(wallet.account().balance(), Matchers.equalTo(10_000L));
        MatcherAssert.assertThat(
            new RsBodyPrint(app.act(new RqFake("GET", "/txn/" + location))).asString(),
            Matchers.containsString("Reversed by")
        );
    }

    @Test
    void returnsNotFound() throws Exception {
        MatcherAssert.assertThat(
            new RsPrint(new TkApp(new TestSource().value()).act(new RqFake("GET", "/nope"))).print(),
            Matchers.startsWith("HTTP/1.1 404")
        );
    }

    /**
     * POST a form and expect a redirect.
     * @param app App
     * @param path Path
     * @param pairs Name, value, name, value...
     * @throws Exception If fails
     */
    private void post(final Take app, final String path, final String... pairs)
        throws Exception {
        MatcherAssert.assertThat(
            path,
            new RsPrint(app.act(this.form(path, pairs))).print(),
            Matchers.startsWith("HTTP/1.1 303")
        );
    }

    /**
     * Fake form request.
     * @param path Path
     * @param pairs Name, value, name, value...
     * @return Request
     */
    private RqFake form(final String path, final String... pairs) {
        final StringBuilder body = new StringBuilder();
        for (int idx = 0; idx < pairs.length; idx += 2) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(pairs[idx]).append('=')
                .append(URLEncoder.encode(pairs[idx + 1], StandardCharsets.UTF_8));
        }
        return new RqFake(
            List.of(
                String.format("POST %s HTTP/1.1", path),
                "Host: localhost",
                "Content-Type: application/x-www-form-urlencoded"
            ),
            body.toString()
        );
    }
}
