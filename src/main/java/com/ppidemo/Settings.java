package com.ppidemo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Settings of one profile, from {@code config/<profile>.properties}.
 *
 * <p>The profile is {@code local} unless {@code -Dppi.profile=NAME} says
 * otherwise. Environment variables win over the file: {@code PPI_DB_URL}
 * over {@code db.url}, {@code PPI_PORT} over {@code port}. A cloud host
 * has no file at all and sets {@code PPI_DB_URL} and {@code PORT}.</p>
 */
public final class Settings {

    /**
     * Profile name, e.g. "local" or "neon".
     */
    private final String profile;

    /**
     * Environment variables.
     */
    private final Map<String, String> env;

    /**
     * Ctor, with the profile from {@code -Dppi.profile}.
     */
    public Settings() {
        this(System.getProperty("ppi.profile", "local"), System.getenv());
    }

    /**
     * Ctor.
     * @param name Profile name
     * @param vars Environment variables
     */
    public Settings(final String name, final Map<String, String> vars) {
        this.profile = name;
        this.env = vars;
    }

    /**
     * Profile name.
     * @return Name
     */
    public String name() {
        return this.profile;
    }

    /**
     * JDBC URL of the database.
     * @return URL
     * @throws IOException If the profile file can't be read
     */
    public String url() throws IOException {
        return this.env.getOrDefault(
            "PPI_DB_URL",
            this.file().getProperty("db.url", "jdbc:postgresql://localhost:5432/ppi_demo")
        );
    }

    /**
     * HTTP port.
     * @return Port
     * @throws IOException If the profile file can't be read
     */
    public int port() throws IOException {
        return Integer.parseInt(
            this.env.getOrDefault(
                "PPI_PORT",
                this.file().getProperty("port", this.env.getOrDefault("PORT", "8080"))
            )
        );
    }

    /**
     * Properties from the profile file; empty if there is no file for
     * the local profile, as in a container.
     * @return Properties
     * @throws IOException If a non-local profile has no file
     */
    private Properties file() throws IOException {
        final Path path = Path.of("config", String.format("%s.properties", this.profile));
        final Properties props = new Properties();
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                props.load(input);
            }
        } else if (!"local".equals(this.profile)) {
            throw new IOException(
                String.format(
                    "%s not found; copy config/%s.properties.example to it and fill it in",
                    path, this.profile
                )
            );
        }
        return props;
    }
}
