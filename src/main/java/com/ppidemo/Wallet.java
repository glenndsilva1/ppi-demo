package com.ppidemo;

import java.io.IOException;

/**
 * Customer wallet: one balance, used by every channel.
 */
public interface Wallet {

    /**
     * Unique number.
     * @return Number
     */
    long id();

    /**
     * Name of the customer.
     * @return Name
     * @throws IOException If fails
     */
    String holder() throws IOException;

    /**
     * Mobile number, 10 digits.
     * @return Mobile
     * @throws IOException If fails
     */
    String mobile() throws IOException;

    /**
     * KYC tier.
     * @return Tier
     * @throws IOException If fails
     */
    Tier tier() throws IOException;

    /**
     * Ledger account that holds the balance.
     * @return Account
     * @throws IOException If fails
     */
    Account account() throws IOException;

    /**
     * Stop all money moving in or out.
     * @throws IOException If fails
     */
    void suspend() throws IOException;

    /**
     * Let money move again.
     * @throws IOException If fails
     */
    void activate() throws IOException;

    /**
     * Move to Full KYC, which raises the balance limit.
     * @throws IOException If fails
     */
    void upgrade() throws IOException;
}
