package com.star.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Runtime configuration. Values are read from environment variables first
 * (the production path, e.g. Docker/EC2) and fall back to a local
 * {@code config.properties} file for development.
 *
 * @param token        Discord bot token
 * @param databasePath location of the SQLite database file
 */
public record BotConfig(String token, Path databasePath) {

    private static final String CONFIG_FILE = "config.properties";

    public static BotConfig load() {
        Properties fileProps = loadFileProperties();

        String token = firstNonBlank(System.getenv("BOT_TOKEN"), fileProps.getProperty("BOT_TOKEN"));
        if (token == null) {
            throw new IllegalStateException(
                    "No bot token found. Set the BOT_TOKEN environment variable or add BOT_TOKEN to " + CONFIG_FILE);
        }

        String dbPath = firstNonBlank(System.getenv("STAR_DB"), fileProps.getProperty("STAR_DB"));
        return new BotConfig(token, Path.of(dbPath != null ? dbPath : "star.db"));
    }

    private static Properties loadFileProperties() {
        Properties props = new Properties();
        if (Files.exists(Path.of(CONFIG_FILE))) {
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + CONFIG_FILE, e);
            }
        }
        return props;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return (b != null && !b.isBlank()) ? b : null;
    }
}
