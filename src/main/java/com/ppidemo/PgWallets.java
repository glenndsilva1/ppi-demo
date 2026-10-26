package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Wallets in PostgreSQL.
 */
public final class PgWallets implements Wallets {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgWallets(final DataSource src) {
        this.source = src;
    }

    @Override
    public Wallet get(final long id) throws IOException {
        final List<Wallet> found;
        try {
            found = new JdbcSession(this.source)
                .sql("SELECT id FROM wallet WHERE id = ?")
                .set(id)
                .select(new ListOutcome<>(rset -> new PgWallet(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        if (found.isEmpty()) {
            throw new IOException(String.format("Wallet #%d not found", id));
        }
        return found.get(0);
    }

    @Override
    public List<Wallet> all() throws IOException {
        try {
            return new JdbcSession(this.source)
                .sql("SELECT id FROM wallet ORDER BY id")
                .select(new ListOutcome<>(rset -> new PgWallet(this.source, rset.getLong(1))));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Wallet add(final String holder, final String mobile, final Tier tier)
        throws IOException {
        final String name = holder.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter the customer's name");
        }
        if (!mobile.matches("[6-9][0-9]{9}")) {
            throw new IllegalArgumentException(
                "A mobile number has 10 digits and starts with 6, 7, 8 or 9"
            );
        }
        final JdbcSession session = new JdbcSession(this.source).autocommit(false);
        try {
            final boolean taken = !session
                .sql("SELECT id FROM wallet WHERE mobile = ?")
                .set(mobile)
                .select(new ListOutcome<>(rset -> rset.getLong(1)))
                .isEmpty();
            if (taken) {
                session.rollback();
                throw new IllegalArgumentException(
                    String.format("Mobile %s already has a wallet", mobile)
                );
            }
            final long account = session
                .sql(
                    String.join(
                        " ",
                        "INSERT INTO account (code, kind, title, cap)",
                        "VALUES (?, 'LIABILITY', ?, ?) RETURNING id"
                    )
                )
                .set(String.format("WALLET-%s", mobile))
                .set(String.format("%s's wallet", name))
                .set(tier.cap())
                .select(new ListOutcome<>(rset -> rset.getLong(1)))
                .get(0);
            final long id = session
                .sql(
                    "INSERT INTO wallet (holder, mobile, tier, account) VALUES (?, ?, ?, ?) RETURNING id"
                )
                .set(name)
                .set(mobile)
                .set(tier.name())
                .set(account)
                .select(new ListOutcome<>(rset -> rset.getLong(1)))
                .get(0);
            session.commit();
            return new PgWallet(this.source, id);
        } catch (final SQLException ex) {
            throw new IOException("Can't open the wallet", new Rollback(session).after(ex));
        }
    }
}
