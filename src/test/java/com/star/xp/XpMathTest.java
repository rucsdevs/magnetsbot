package com.star.xp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XpMathTest {

    @Test
    void levelZeroUntilFirstThreshold() {
        assertEquals(0, XpMath.levelForXp(0));
        assertEquals(0, XpMath.levelForXp(99));
        assertEquals(1, XpMath.levelForXp(100));
    }

    @Test
    void quadraticCurve() {
        assertEquals(100, XpMath.xpForLevel(1));
        assertEquals(400, XpMath.xpForLevel(2));
        assertEquals(40_000, XpMath.xpForLevel(20));

        assertEquals(2, XpMath.levelForXp(400));
        assertEquals(2, XpMath.levelForXp(899));
        assertEquals(3, XpMath.levelForXp(900));
    }

    @Test
    void levelAndXpForLevelAreInverse() {
        for (int level = 0; level <= 50; level++) {
            long threshold = XpMath.xpForLevel(level);
            assertEquals(level, XpMath.levelForXp(threshold), "at threshold for level " + level);
            if (threshold > 0) {
                assertEquals(level - 1, XpMath.levelForXp(threshold - 1), "just below level " + level);
            }
        }
    }

    @Test
    void xpToNextLevel() {
        assertEquals(100, XpMath.xpToNextLevel(0));
        assertEquals(1, XpMath.xpToNextLevel(99));
        assertEquals(300, XpMath.xpToNextLevel(100));
    }

    @Test
    void negativeInputsHandled() {
        assertEquals(0, XpMath.levelForXp(-50));
        assertThrows(IllegalArgumentException.class, () -> XpMath.xpForLevel(-1));
    }
}
