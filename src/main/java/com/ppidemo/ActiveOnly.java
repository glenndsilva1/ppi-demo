package com.ppidemo;

import java.io.IOException;

/**
 * Ledger that refuses to move money in or out of a suspended account.
 */
public final class ActiveOnly implements Ledger {

    /**
     * Original ledger.
     */
    private final Ledger origin;

    /**
     * Ctor.
     * @param ledger Original ledger
     */
    public ActiveOnly(final Ledger ledger) {
        this.origin = ledger;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        for (final Account account : new Impact(entry.lines()).accounts()) {
            if (!account.active()) {
                throw new IllegalArgumentException(
                    String.format(
                        "%s is suspended, so no money can move in or out of it",
                        account.title()
                    )
                );
            }
        }
        return this.origin.post(entry);
    }
}
