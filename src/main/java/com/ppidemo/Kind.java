package com.ppidemo;

/**
 * Kind of a ledger account; defines which side increases its balance.
 */
public enum Kind {

    /**
     * Asset, e.g. the bank pool. Debits increase it.
     */
    ASSET(true),

    /**
     * Liability, e.g. a customer wallet. Credits increase it.
     */
    LIABILITY(false),

    /**
     * Equity. Credits increase it.
     */
    EQUITY(false),

    /**
     * Income, e.g. fees. Credits increase it.
     */
    INCOME(false),

    /**
     * Expense. Debits increase it.
     */
    EXPENSE(true);

    /**
     * Is it a debit-normal account?
     */
    private final boolean debit;

    /**
     * Ctor.
     * @param dbt Debit-normal
     */
    Kind(final boolean dbt) {
        this.debit = dbt;
    }

    /**
     * Balance, signed by the normal side of the account.
     * @param debits Total debits, in paise
     * @param credits Total credits, in paise
     * @return Balance, in paise
     */
    public long balance(final long debits, final long credits) {
        final long balance;
        if (this.debit) {
            balance = Math.subtractExact(debits, credits);
        } else {
            balance = Math.subtractExact(credits, debits);
        }
        return balance;
    }
}
