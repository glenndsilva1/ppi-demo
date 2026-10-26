package com.ppidemo;

/**
 * Journal line: one side of a transaction against one account.
 */
public interface Line {

    /**
     * Account.
     * @return Account
     */
    Account account();

    /**
     * Side.
     * @return Debit or credit
     */
    Side side();

    /**
     * Amount, always positive.
     * @return Amount, in paise
     */
    long amount();
}
