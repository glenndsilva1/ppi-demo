package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Agents in PostgreSQL.
 */
public final class PgAgents implements Agents {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgAgents(final DataSource src) {
        this.source = src;
    }

    @Override
    public Agent get(final long id) throws IOException {
        final List<Agent> found;
        try {
            found = new JdbcSession(this.source)
                .sql("SELECT id FROM agent WHERE id = ?")
                .set(id)
                .select(new ListOutcome<>(rset -> new PgAgent(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Agent #%d not found", id));
        }
        return found.get(0);
    }

    @Override
    public List<Agent> all() throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql("SELECT id FROM agent ORDER BY id")
                .select(new ListOutcome<>(rset -> new PgAgent(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Agent add(final String name, final String place) throws IOException {
        final String shop = name.trim();
        if (shop.isEmpty()) {
            throw new IllegalArgumentException("Enter the agent's shop name");
        }
        final JdbcSession session = new JdbcSession(this.source).autocommit(false);
        try {
            final long id = session
                .sql("SELECT nextval('agent_id_seq')")
                .select(new ListOutcome<>(rset -> rset.getLong(1)))
                .get(0);
            final long cash = this.account(
                session, String.format("FLOAT-%d", id), String.format("%s's float", shop)
            );
            final long earned = this.account(
                session, String.format("COMMISSION-%d", id),
                String.format("Commission owed to %s", shop)
            );
            session
                .sql(
                    String.join(
                        " ",
                        "INSERT INTO agent (id, name, place, float_account, commission_account)",
                        "VALUES (?, ?, ?, ?, ?)"
                    )
                )
                .set(id)
                .set(shop)
                .set(place.trim())
                .set(cash)
                .set(earned)
                .execute();
            session.commit();
            return new PgAgent(this.source, id);
        } catch (final SQLException ex) {
            throw new IOException("Can't onboard the agent", new Rollback(session).after(ex));
        }
    }

    /**
     * Open a liability account within the session.
     * @param session Session, not committed
     * @param code Code
     * @param title Title
     * @return Account number
     * @throws SQLException If fails
     */
    private long account(final JdbcSession session, final String code, final String title)
        throws SQLException {
        return session
            .sql("INSERT INTO account (code, kind, title) VALUES (?, 'LIABILITY', ?) RETURNING id")
            .set(code)
            .set(title)
            .select(new ListOutcome<>(rset -> rset.getLong(1)))
            .get(0);
    }
}
