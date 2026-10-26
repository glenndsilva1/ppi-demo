package com.ppidemo.web;

import com.ppidemo.Agent;
import com.ppidemo.Agents;
import com.ppidemo.Rupees;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

/**
 * BC agents: their float, their commission, and a way to top up.
 */
public final class TkAgents implements Take {

    /**
     * Agents.
     */
    private final Agents agents;

    /**
     * Ctor.
     * @param agts Agents
     */
    public TkAgents(final Agents agts) {
        this.agents = agts;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final StringBuilder html = new StringBuilder(4096);
        html.append("<h1>BC agents</h1>")
            .append("<p class='lead'>A BC agent is a local shop that takes and gives cash for customers. ")
            .append("The agent first deposits money with the bank (<b>float</b>). A cash-in moves value ")
            .append("from the agent's float into the customer's wallet, so the agent can never hand out ")
            .append("more e-money than they prefunded. Each cash service earns <b>0.5% commission</b>.</p>")
            .append("<div class='wide'><table><tr><th>Agent</th><th class='num'>Float left</th>")
            .append("<th class='num'>Commission earned</th><th>Top up float</th></tr>");
        for (final Agent agent : this.agents.all()) {
            html.append("<tr>")
                .append(
                    String.format(
                        "<td><b>%s</b><div class='muted small'>%s</div></td>",
                        new Escaped(agent.name()), new Escaped(agent.place())
                    )
                )
                .append(String.format("<td class='num big'>%s</td>", new Rupees(agent.cash().balance())))
                .append(String.format("<td class='num'>%s</td>", new Rupees(agent.commission().balance())))
                .append(
                    String.format(
                        String.join(
                            "",
                            "<td><form method='post' action='/agents/%d/float' style='display:flex;gap:8px;align-items:end'>",
                            "<input name='amount' value='5000' inputmode='decimal' aria-label='Amount in rupees' style='max-width:120px'>",
                            "<button style='margin:0'>Deposit</button></form></td>"
                        ),
                        agent.id()
                    )
                )
                .append("</tr>");
        }
        html.append("</table></div>")
            .append("<h2>Onboard an agent</h2>")
            .append("<form class='card' method='post' action='/agents' style='max-width:420px'>")
            .append("<label>Shop name<input name='name' required placeholder='e.g. Mahalaxmi Mobile Shop'></label>")
            .append("<label>Town or area<input name='place' required placeholder='e.g. Satara'></label>")
            .append("<button>Onboard agent</button></form>");
        return new RsPage("BC agents", html);
    }
}
