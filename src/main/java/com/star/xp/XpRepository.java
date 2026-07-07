package com.star.xp;

import com.star.db.Database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistence for {@link XpProfile}s. All methods are synchronized on the
 * shared connection because JDA delivers events on multiple threads.
 */
public final class XpRepository {

    private final Database db;

    public XpRepository(Database db) {
        this.db = db;
    }

    public Optional<XpProfile> find(String guildId, String userId) {
        String sql = "SELECT xp, messages, last_award FROM xp_profiles WHERE guild_id = ? AND user_id = ?";
        synchronized (db) {
            try (PreparedStatement stmt = db.connection().prepareStatement(sql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new XpProfile(
                            guildId, userId, rs.getLong("xp"), rs.getLong("messages"), rs.getLong("last_award")));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to load profile", e);
            }
        }
    }

    public void save(XpProfile profile) {
        String sql = """
                INSERT INTO xp_profiles (guild_id, user_id, xp, messages, last_award)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (guild_id, user_id)
                DO UPDATE SET xp = excluded.xp, messages = excluded.messages, last_award = excluded.last_award
                """;
        synchronized (db) {
            try (PreparedStatement stmt = db.connection().prepareStatement(sql)) {
                stmt.setString(1, profile.guildId());
                stmt.setString(2, profile.userId());
                stmt.setLong(3, profile.xp());
                stmt.setLong(4, profile.messages());
                stmt.setLong(5, profile.lastAwardMs());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to save profile", e);
            }
        }
    }

    /** Top {@code limit} profiles in a guild, highest XP first. */
    public List<XpProfile> topByGuild(String guildId, int limit) {
        String sql = """
                SELECT user_id, xp, messages, last_award FROM xp_profiles
                WHERE guild_id = ? ORDER BY xp DESC LIMIT ?
                """;
        synchronized (db) {
            try (PreparedStatement stmt = db.connection().prepareStatement(sql)) {
                stmt.setString(1, guildId);
                stmt.setInt(2, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<XpProfile> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(new XpProfile(guildId, rs.getString("user_id"),
                                rs.getLong("xp"), rs.getLong("messages"), rs.getLong("last_award")));
                    }
                    return results;
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to load leaderboard", e);
            }
        }
    }

    /** 1-based rank of a user within a guild (ties share the better rank). */
    public int rank(String guildId, String userId) {
        String sql = """
                SELECT 1 + COUNT(*) AS rank FROM xp_profiles
                WHERE guild_id = ? AND xp > (SELECT xp FROM xp_profiles WHERE guild_id = ? AND user_id = ?)
                """;
        synchronized (db) {
            try (PreparedStatement stmt = db.connection().prepareStatement(sql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, guildId);
                stmt.setString(3, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt("rank") : 0;
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to compute rank", e);
            }
        }
    }
}
