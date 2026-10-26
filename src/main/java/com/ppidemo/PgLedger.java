package com.ppidemo;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;
import javax.sql.DataSource;

/**
 * Ledger in PostgreSQL. Writes a transaction and its lines in one
 * database transaction; the database itself refuses unbalanced ones and
 * ones that break an account's controls.
 *
 * <p>Touched accounts are locked first, in order of their ids, so two
 * payments from one wallet can't both pass the balance check.</p>
 *
 * <p>Use it through {@link ControlledLedger}, never alone.</p>
 */
public final class PgLedger implements Ledger {

    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Ctor.
     * @param src Data source
     */
    public PgLedger(final DataSource src) {
        this.source = src;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        final List<Line> lines = entry.lines();
        final TreeSet<Long> touched = new TreeSet<>();
        for (final Line line : lines) {
            touched.add(line.account().id());
        }
        final JdbcSession session = new JdbcSession(this.source).autocommit(false);
        try {
            for (final long account : touched) {
                session.sql("SELECT id FROM account WHERE id = ? FOR UPDATE")
                    .set(account)
                    .select(new ListOutcome<>(rset -> rset.getLong(1)));
            }
            final long txn;
            if (entry.reversed().isEmpty()) {
                txn = session
                    .sql("INSERT INTO txn (operation, memo) VALUES (?, ?) RETURNING id")
                    .set(entry.operation().name())
                    .set(entry.memo())
                    .select(new ListOutcome<>(rset -> rset.getLong(1)))
                    .get(0);
            } else {
                txn = session
                    .sql(
                        "INSERT INTO txn (operation, memo, reverses) VALUES (?, ?, ?) RETURNING id"
                    )
                    .set(entry.operation().name())
                    .set(entry.memo())
                    .set(entry.reversed().get(0).id())
                    .select(new ListOutcome<>(rset -> rset.getLong(1)))
                    .get(0);
            }
            for (final Line line : lines) {
                session
                    .sql("INSERT INTO line (txn, account, side, amount) VALUES (?, ?, ?, ?)")
                    .set(txn)
                    .set(line.account().id())
                    .set(line.side().code())
                    .set(line.amount())
                    .execute();
            }
            session.commit();
            return new PgTxn(this.source, txn);
        } catch (final SQLException ex) {
            throw new IOException(
                String.format("Can't post %s", entry.operation().title()),
                new Rollback(session).after(ex)
            );
        }
    }
}
