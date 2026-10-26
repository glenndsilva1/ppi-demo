package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * Ledger that refuses a transaction unless it has two or more lines,
 * every amount is positive, and debits equal credits.
 */
public final class BalancedLedger implements Ledger {

    /**
     * Original ledger.
     */
    private final Ledger origin;

    /**
     * Ctor.
     * @param ledger Original ledger
     */
    public BalancedLedger(final Ledger ledger) {
        this.origin = ledger;
    }

    @Override
    public Txn post(final Entry entry) throws IOException {
        final List<Line> lines = entry.lines();
        if (lines.size() < 2) {
            throw new IllegalArgumentException(
                String.format("A transaction needs 2+ lines, %d given", lines.size())
            );
        }
        long debits = 0L;
        long credits = 0L;
        for (final Line line : lines) {
            if (line.amount() <= 0L) {
                throw new IllegalArgumentException("Amount must be more than zero");
            }
            if (line.side() == Side.DEBIT) {
                debits = Math.addExact(debits, line.amount());
            } else {
                credits = Math.addExact(credits, line.amount());
            }
        }
        if (debits != credits) {
            throw new IllegalArgumentException(
                String.format(
                    "Debits %s don't equal credits %s", new Rupees(debits), new Rupees(credits)
                )
            );
        }
        return this.origin.post(entry);
    }
}
