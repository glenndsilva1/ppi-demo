package com.ppidemo;

import java.io.IOException;

/**
 * BC agent (business correspondent): a shop that takes and gives cash
 * for customers, against a float they prefunded.
 */
public interface Agent {

    /**
     * Unique number.
     * @return Number
     */
    long id();

    /**
     * Name of the shop.
     * @return Name
     * @throws IOException If fails
     */
    String name() throws IOException;

    /**
     * Where the shop is.
     * @return Town or area
     * @throws IOException If fails
     */
    String place() throws IOException;

    /**
     * Prefunded float: what the agent can still credit to wallets.
     * @return Account
     * @throws IOException If fails
     */
    Account cash() throws IOException;

    /**
     * Commission earned and not paid out yet.
     * @return Account
     * @throws IOException If fails
     */
    Account commission() throws IOException;
}
