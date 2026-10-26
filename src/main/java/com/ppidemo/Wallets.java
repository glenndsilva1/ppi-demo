package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * All customer wallets.
 */
public interface Wallets {

    /**
     * Find by number.
     * @param id Number
     * @return Wallet
     * @throws IOException If not found
     */
    Wallet get(long id) throws IOException;

    /**
     * All of them, oldest first.
     * @return Wallets
     * @throws IOException If fails
     */
    List<Wallet> all() throws IOException;

    /**
     * Open a wallet, with its own ledger account.
     * @param holder Name of the customer
     * @param mobile Mobile number, 10 digits
     * @param tier KYC tier
     * @return Wallet
     * @throws IOException If fails
     */
    Wallet add(String holder, String mobile, Tier tier) throws IOException;
}
