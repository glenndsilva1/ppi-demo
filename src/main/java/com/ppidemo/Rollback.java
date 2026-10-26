package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import java.sql.SQLException;

/**
 * Rollback of a failed jcabi-jdbc transaction.
 *
 * <p>When a statement fails, jcabi-jdbc rolls back and closes the
 * connection itself; when COMMIT fails, the connection stays open and
 * must be rolled back here. Both cases end with nothing written.</p>
 */
final class Rollback {

    /**
     * Session.
     */
    private final JdbcSession session;

    /**
     * Ctor.
     * @param sess Session that failed
     */
    Rollback(final JdbcSession sess) {
        this.session = sess;
    }

    /**
     * Roll back, if still needed.
     * @param cause Why the transaction failed
     * @return The same cause, for rethrowing
     */
    SQLException after(final SQLException cause) {
        try {
            this.session.rollback();
        } catch (final IllegalStateException closed) {
            assert closed != null;
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
        return cause;
    }
}
