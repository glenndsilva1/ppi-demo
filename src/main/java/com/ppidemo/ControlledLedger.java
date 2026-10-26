package com.ppidemo;

import java.io.IOException;
import javax.sql.DataSource;

/**
 * The one ledger every channel uses, with every control in front of it.
 */
public final class ControlledLedger implements Ledger {

    /**
     * Original ledger, decorated.
     */
    private final Ledger origin;

    /**
     * Ctor.
     * @param src Data source
     */
    public ControlledLedger(final DataSource src) {
        this(
            new BalancedLedger(
                new ActiveOnly(
                    new NoOverdraft(
                        new WithinCap(
                            new PgLedger(src)
                        )
                    )
                )
            )
        );
    }

    /**
     * Ctor.
     * @param ledger Ledger, decorated already
     */
    private ControlledLedger(final Ledger ledger) {
        this.origin = ledger;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        return this.origin.post(entry);
    }
}
