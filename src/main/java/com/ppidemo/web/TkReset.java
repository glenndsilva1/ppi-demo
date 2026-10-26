package com.ppidemo.web;

import com.ppidemo.Demo;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsRedirect;

/**
 * Throw everything away and load the demo data again.
 */
public final class TkReset implements Take {

    /**
     * Demo.
     */
    private final Demo demo;

    /**
     * Ctor.
     * @param dmo Demo
     */
    public TkReset(final Demo dmo) {
        this.demo = dmo;
    }

    @Override
    public Response act(final Request req) throws Exception {
        this.demo.reset();
        return new RsRedirect("/");
    }
}
