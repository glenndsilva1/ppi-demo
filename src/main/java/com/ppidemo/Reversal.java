package com.ppidemo;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Entry that undoes an earlier transaction: the same lines, sides swapped.
 */
public final class Reversal implements Entry {

    /**
     * Transaction to undo.
     */
    private final Txn origin;

    /**
     * Ctor.
     * @param txn Transaction to undo
     */
    public Reversal(final Txn txn) {
        this.origin = txn;
    }

    @Override
    public Operation operation() {
        return Operation.REVERSAL;
    }

    @Override
    public String memo() throws IOException {
        return String.format(
            "Undoes %s %s", new Receipt(this.origin.id()), this.origin.operation().title()
        );
    }

    @Override
    public List<Line> lines() throws IOException {
        if (this.origin.operation() == Operation.REVERSAL) {
            throw new IllegalArgumentException("A reversal can't be reversed");
        }
        if (!this.origin.reversedBy().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "%s was already reversed by %s",
                    new Receipt(this.origin.id()), new Receipt(this.origin.reversedBy().get(0))
                )
            );
        }
        final List<Line> lines = new LinkedList<>();
        for (final Line line : this.origin.lines()) {
            if (line.side() == Side.DEBIT) {
                lines.add(new Credit(line.account(), line.amount()));
            } else {
                lines.add(new Debit(line.account(), line.amount()));
            }
        }
        return lines;
    }

    @Override
    public List<Txn> reversed() {
        return List.of(this.origin);
    }
}
