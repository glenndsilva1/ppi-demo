package com.ppidemo;

import java.io.IOException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Migrated test database, from the {@code ppi.db.url} system property.
 */
public final class TestSource {

    /**
     * The data source, migrated.
     * @return Data source
     * @throws IOException If fails
     */
    public DataSource value() throws IOException {
        final PGSimpleDataSource src = new PGSimpleDataSource();
        src.setUrl(
            System.getProperty("ppi.db.url", "jdbc:postgresql://localhost:5432/ppi_demo_test")
        );
        new PgMigrations(src).apply();
        return src;
    }
}
