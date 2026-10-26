package com.ppidemo;

import java.io.IOException;

/**
 * Ledger that refuses to take any account above its cap, e.g. the
 * balance limit of a Minimum-KYC wallet.
 */
public final class WithinCap implements Ledger {

    /**
     * Original ledger.
     */
    private final Ledger origin;

    /**
     * Ctor.
     * @param ledger Original ledger
     */
    public WithinCap(final Ledger ledger) {
        this.origin = ledger;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        final Impact impact = new Impact(entry.lines());
        for (final Account account : impact.accounts()) {
            final long change = impact.on(account);
            final long balance = account.balance();
            if (change > 0L && balance + change > account.cap()) {
                throw new IllegalArgumentException(
                    String.format(
                        "Over the limit: %s can hold at most %s; it has %s and this adds %s",
                        account.title(), new Rupees(account.cap()),
                        new Rupees(balance), new Rupees(change)
                    )
                );
            }
        }
        return this.origin.post(entry);
    }
}
