package com.ppidemo;

import java.io.IOException;

/**
 * Ledger that refuses to take any account below zero: a wallet can't
 * spend more than it has, an agent can't hand out more than their float.
 */
public final class NoOverdraft implements Ledger {

    /**
     * Original ledger.
     */
    private final Ledger origin;

    /**
     * Ctor.
     * @param ledger Original ledger
     */
    public NoOverdraft(final Ledger ledger) {
        this.origin = ledger;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        final Impact impact = new Impact(entry.lines());
        for (final Account account : impact.accounts()) {
            final long change = impact.on(account);
            final long balance = account.balance();
            if (balance + change < 0L) {
                throw new IllegalArgumentException(
                    String.format(
                        "Not enough money: %s has %s, this needs %s",
                        account.title(), new Rupees(balance), new Rupees(-change)
                    )
                );
            }
        }
        return this.origin.post(entry);
    }
}
