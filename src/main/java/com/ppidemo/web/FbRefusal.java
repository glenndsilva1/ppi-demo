package com.ppidemo.web;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.RqFallback;
import org.takes.misc.Opt;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsWithStatus;

/**
 * Fallback that turns a broken business rule into a readable 400 page.
 *
 * <p>A rule is broken when Java refuses the input
 * ({@link IllegalArgumentException}), or when PostgreSQL refuses it with
 * a constraint (SQLSTATE class 23) or a rule raised by a trigger
 * (SQLSTATE P0001). Anything else is not handled here.</p>
 */
public final class FbRefusal implements Fallback {

    @Override
    public Opt<org.takes.Response> route(final RqFallback req) throws Exception {
        String reason = "";
        for (Throwable err = req.throwable(); err != null; err = err.getCause()) {
            if (err instanceof IllegalArgumentException) {
                reason = err.getMessage();
                break;
            }
            if (err instanceof PSQLException && this.rule((PSQLException) err)) {
                reason = this.text((PSQLException) err);
                break;
            }
        }
        final Opt<org.takes.Response> response;
        if (reason.isEmpty()) {
            response = new Opt.Empty<>();
        } else {
            response = new Opt.Single<>(
                new RsWithStatus(
                    new RsPage(
                        "Refused",
                        String.join(
                            "",
                            "<h1>Refused</h1>",
                            String.format("<div class='notice bad'>%s</div>", new Escaped(reason)),
                            "<p class='muted'>Nothing was posted: no money moved and no balance ",
                            "changed. The same controls apply to every channel.</p>",
                            String.format(
                                "<p><a href='%s'><b>← Go back</b></a></p>",
                                new Escaped(new RqHeaders.Smart(req).single("Referer", "/"))
                            )
                        )
                    ),
                    400
                )
            );
        }
        return response;
    }

    /**
     * Is it a broken rule rather than a failure?
     * @param err Error
     * @return TRUE if a constraint or a trigger refused the change
     */
    private boolean rule(final PSQLException err) {
        final String state = String.valueOf(err.getSQLState());
        return state.startsWith("23") || "P0001".equals(state);
    }

    /**
     * Message for a human.
     * @param err Error
     * @return Text
     */
    private String text(final PSQLException err) {
        final ServerErrorMessage msg = err.getServerErrorMessage();
        final String text;
        if (msg == null) {
            text = err.getMessage();
        } else if (msg.getDetail() == null) {
            text = msg.getMessage();
        } else {
            text = String.format("%s. %s", msg.getMessage(), msg.getDetail());
        }
        return text;
    }
}
