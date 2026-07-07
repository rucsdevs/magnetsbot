package com.star.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the single SQLite connection and applies the schema on startup.
 *
 * <p>SQLite serializes writes internally, and JDA dispatches events from a
 * thread pool, so all access goes through this one connection which callers
 * must synchronize on (see {@link com.star.xp.XpRepository}).
 */
public final class Database implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS xp_profiles (
                guild_id   TEXT    NOT NULL,
                user_id    TEXT    NOT NULL,
                xp         INTEGER NOT NULL DEFAULT 0,
                messages   INTEGER NOT NULL DEFAULT 0,
                last_award INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (guild_id, user_id)
            );
            CREATE INDEX IF NOT EXISTS idx_xp_guild_rank ON xp_profiles (guild_id, xp DESC);
            """;

    private final Connection connection;

    private Database(Connection connection) {
        this.connection = connection;
    }

    /** Opens (or creates) the database at the given path and applies the schema. */
    public static Database open(Path path) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            applySchema(conn);
            log.info("Database ready at {}", path.toAbsolutePath());
            return new Database(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open database at " + path, e);
        }
    }

    /** In-memory database for tests. */
    public static Database inMemory() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            applySchema(conn);
            return new Database(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open in-memory database", e);
        }
    }

    private static void applySchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            for (String ddl : SCHEMA.split(";")) {
                if (!ddl.isBlank()) {
                    stmt.execute(ddl);
                }
            }
        }
    }

    public Connection connection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("Error closing database", e);
        }
    }
}
