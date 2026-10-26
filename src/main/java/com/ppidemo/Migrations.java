package com.ppidemo;

import java.io.IOException;

/**
 * Database schema changes.
 */
public interface Migrations {

    /**
     * Apply all changes not applied yet.
     * @throws IOException If fails
     */
    void apply() throws IOException;
}
