package com.ppidemo;

import com.ppidemo.web.TkApp;
import java.io.IOException;
import org.postgresql.ds.PGSimpleDataSource;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

/**
 * Entry point: migrates the database, fills in demo data, then serves HTTP.
 *
 * <p>Settings come from {@code config/<profile>.properties}, see
 * {@link Settings}.</p>
 */
public final class Main {

    /**
     * JDBC URL.
     */
    private final String url;

    /**
     * HTTP port.
     */
    private final int port;

    /**
     * Ctor.
     * @param jdbc JDBC URL
     * @param prt HTTP port
     */
    public Main(final String jdbc, final int prt) {
        this.url = jdbc;
        this.port = prt;
    }

    /**
     * Java entry point.
     * @param argv Arguments, ignored
     * @throws IOException If fails
     */
    public static void main(final String... argv) throws IOException {
        final Settings settings = new Settings();
        System.out.printf("Profile %s%n", settings.name());
        new Main(settings.url(), settings.port()).exec();
    }

    /**
     * Migrate and serve, until the process is killed.
     * @throws IOException If fails
     */
    public void exec() throws IOException {
        final PGSimpleDataSource src = new PGSimpleDataSource();
        src.setUrl(this.url);
        new PgMigrations(src).apply();
        new Demo(src).fill();
        System.out.println("Database is up to date");
        System.out.printf("Listening on http://localhost:%d/%n", this.port);
        new FtBasic(new TkApp(src), this.port).start(Exit.NEVER);
    }
}
