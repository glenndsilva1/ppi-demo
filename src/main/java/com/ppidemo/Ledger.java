package com.ppidemo;

import java.io.IOException;

/**
 * General ledger: the only way money moves.
 */
public interface Ledger {

    /**
     * Post a transaction, atomically.
     * @param entry What to post
     * @return Posted transaction
     * @throws IOException If fails; nothing is posted then
     */
    Txn post(Entry entry) throws IOException;
}
