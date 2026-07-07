package com.star.xp;

/**
 * A member's XP state within one guild.
 *
 * @param guildId       guild the profile belongs to
 * @param userId        member the profile belongs to
 * @param xp            total accumulated XP
 * @param messages      number of messages that earned XP
 * @param lastAwardMs   epoch millis of the last XP award, used for cooldowns
 */
public record XpProfile(String guildId, String userId, long xp, long messages, long lastAwardMs) {

    public static XpProfile empty(String guildId, String userId) {
        return new XpProfile(guildId, userId, 0, 0, 0);
    }

    public int level() {
        return XpMath.levelForXp(xp);
    }
}
