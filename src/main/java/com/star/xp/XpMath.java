package com.star.xp;

/**
 * The leveling curve: reaching level {@code n} requires {@code 100 * n^2}
 * total XP. Quadratic growth keeps early levels fast (level 1 at 100 XP)
 * while making high levels a long-term goal (level 20 at 40,000 XP).
 */
public final class XpMath {

    public static int levelForXp(long xp) {
        if (xp < 0) {
            return 0;
        }
        return (int) Math.floor(Math.sqrt(xp / 100.0));
    }

    /** Total XP required to reach the given level. */
    public static long xpForLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("level must be non-negative");
        }
        return 100L * level * level;
    }

    /** XP still needed to advance from {@code xp} to the next level. */
    public static long xpToNextLevel(long xp) {
        return xpForLevel(levelForXp(xp) + 1) - xp;
    }

    private XpMath() {
    }
}
