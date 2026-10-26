package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * All ledger accounts.
 */
public interface Accounts {

    /**
     * Find by code.
     * @param code Code, e.g. "BANK_POOL"
     * @return Account
     * @throws IOException If not found
     */
    Account get(String code) throws IOException;

    /**
     * All of them, system accounts first.
     * @return Accounts
     * @throws IOException If fails
     */
    List<Account> all() throws IOException;
}
