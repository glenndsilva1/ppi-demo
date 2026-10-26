package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * All BC agents.
 */
public interface Agents {

    /**
     * Find by number.
     * @param id Number
     * @return Agent
     * @throws IOException If not found
     */
    Agent get(long id) throws IOException;

    /**
     * All of them, oldest first.
     * @return Agents
     * @throws IOException If fails
     */
    List<Agent> all() throws IOException;

    /**
     * Onboard an agent, with a float account and a commission account.
     * @param name Name of the shop
     * @param place Town or area
     * @return Agent
     * @throws IOException If fails
     */
    Agent add(String name, String place) throws IOException;
}
