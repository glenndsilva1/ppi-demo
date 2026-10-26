package com.ppidemo;

/**
 * Credit line.
 */
public final class Credit implements Line {

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
     * @param account Account to credit
     * @param amount Amount, in paise
     */
    public Credit(final Account account, final long amount) {
        this.acc = account;
        this.paise = amount;
    }

    @Override
    public Account account() {
        return this.acc;
    }

    @Override
    public Side side() {
        return Side.CREDIT;
    }

    @Override
    public long amount() {
        return this.paise;
    }
}
