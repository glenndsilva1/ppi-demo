package com.ppidemo;

/**
 * Debit line.
 */
public final class Debit implements Line {

    /**
     * Account.
     */
    private final Account acc;

    /**
     * Amount, in paise.
     */
    private final long paise;

    /**
     * Ctor.
     * @param account Account to debit
     * @param amount Amount, in paise
     */
    public Debit(final Account account, final long amount) {
        this.acc = account;
        this.paise = amount;
    }

    @Override
    public Account account() {
        return this.acc;
    }

    @Override
    public Side side() {
        return Side.DEBIT;
    }

    @Override
    public long amount() {
        return this.paise;
    }
}
