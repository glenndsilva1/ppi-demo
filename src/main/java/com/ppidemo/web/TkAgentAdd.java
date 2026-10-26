package com.ppidemo.web;

import com.ppidemo.Agents;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsRedirect;

/**
 * Onboard an agent.
 */
public final class TkAgentAdd implements Take {

    /**
     * Agents.
     */
    private final Agents agents;

    /**
     * Ctor.
     * @param agts Agents
     */
    public TkAgentAdd(final Agents agts) {
        this.agents = agts;
    }

    @Override
    public Response act(final Request req) throws Exception {
        final RqFormSmart form = new RqFormSmart(req);
        this.agents.add(form.single("name"), form.single("place"));
        return new RsRedirect("/agents");
    }
}
