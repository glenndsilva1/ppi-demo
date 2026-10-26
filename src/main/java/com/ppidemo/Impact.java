package com.ppidemo;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * How a set of journal lines changes each account's balance.
 */
public final class Impact {

    /**
     * Lines.
     */
    private final List<Line> lines;

    /**
     * Ctor.
     * @param lns Lines
     */
    public Impact(final List<Line> lns) {
        this.lines = lns;
    }

    /**
     * Accounts touched, each once, in order of first appearance.
     * @return Accounts
     */
    public List<Account> accounts() {
        final Map<Long, Account> found = new LinkedHashMap<>();
        for (final Line line : this.lines) {
            found.putIfAbsent(line.account().id(), line.account());
        }
        return new LinkedList<>(found.values());
    }

    /**
     * Change of one account's balance.
     * @param account Account
     * @return Change, in paise; positive when the balance goes up
     * @throws IOException If fails
     */
    public long on(final Account account) throws IOException {
        long debits = 0L;
        long credits = 0L;
        for (final Line line : this.lines) {
            if (line.account().id() == account.id()) {
                if (line.side() == Side.DEBIT) {
                    debits = Math.addExact(debits, line.amount());
                } else {
                    credits = Math.addExact(credits, line.amount());
                }
            }
        }
        return account.kind().balance(debits, credits);
    }
}
