package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * Posted transactions.
 */
public interface Txns {

    /**
     * Find by number.
     * @param id Number
     * @return Transaction
     * @throws IOException If not found
     */
    Txn get(long id) throws IOException;

    /**
     * Most recent first.
     * @param limit How many, at most
     * @return Transactions
     * @throws IOException If fails
     */
    List<Txn> recent(int limit) throws IOException;

    /**
     * Most recent first, only those that touch one account.
     * @param account Account
     * @param limit How many, at most
     * @return Transactions
     * @throws IOException If fails
     */
    List<Txn> touching(Account account, int limit) throws IOException;
}
