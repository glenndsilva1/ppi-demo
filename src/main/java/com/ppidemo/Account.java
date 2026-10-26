package com.ppidemo;

import java.io.IOException;

/**
 * Ledger account.
 */
public interface Account {

    /**
     * Unique number.
     * @return Number
     */
    long id();

    /**
     * Unique code, e.g. "BANK_POOL".
     * @return Code
     * @throws IOException If fails
     */
    String code() throws IOException;

    /**
     * Human-readable title, e.g. "Asha Patil's wallet".
     * @return Title
     * @throws IOException If fails
     */
    String title() throws IOException;

    /**
     * Kind.
     * @return Kind
     * @throws IOException If fails
     */
    Kind kind() throws IOException;

    /**
     * Current balance, signed by the normal side of the account.
     * @return Balance, in paise
     * @throws IOException If fails
     */
    long balance() throws IOException;

    /**
     * The most this account may hold.
     * @return Cap, in paise
     * @throws IOException If fails
     */
    long cap() throws IOException;

    /**
     * Can money move in or out?
     * @return FALSE if suspended
     * @throws IOException If fails
     */
    boolean active() throws IOException;
}
