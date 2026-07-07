package com.star.xp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.star.db.Database;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.OptionalInt;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XpServiceTest {

    /** Clock whose time is advanced manually by tests. */
    private static final class MutableClock extends Clock {
        private long millis = 1_000_000;

        void advance(long ms) {
            millis += ms;
        }

        @Override
        public long millis() {
            return millis;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    private Database db;
    private XpRepository repository;
    private MutableClock clock;
    private XpService service;

    @BeforeEach
    void setUp() {
        db = Database.inMemory();
        repository = new XpRepository(db);
        clock = new MutableClock();
        // Random with fixed seed keeps awards deterministic within a test run
        service = new XpService(repository, clock, new Random(42));
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void firstMessageCreatesProfileAndAwardsXp() {
        service.recordMessage("g1", "u1");

        XpProfile profile = repository.find("g1", "u1").orElseThrow();
        assertTrue(profile.xp() >= XpService.MIN_AWARD && profile.xp() <= XpService.MAX_AWARD);
        assertEquals(1, profile.messages());
    }

    @Test
    void messagesWithinCooldownEarnNothing() {
        service.recordMessage("g1", "u1");
        long xpAfterFirst = repository.find("g1", "u1").orElseThrow().xp();

        clock.advance(XpService.COOLDOWN_MS - 1);
        service.recordMessage("g1", "u1");

        XpProfile profile = repository.find("g1", "u1").orElseThrow();
        assertEquals(xpAfterFirst, profile.xp());
        assertEquals(1, profile.messages());
    }

    @Test
    void messagesAfterCooldownEarnXp() {
        service.recordMessage("g1", "u1");
        long xpAfterFirst = repository.find("g1", "u1").orElseThrow().xp();

        clock.advance(XpService.COOLDOWN_MS);
        service.recordMessage("g1", "u1");

        XpProfile profile = repository.find("g1", "u1").orElseThrow();
        assertTrue(profile.xp() > xpAfterFirst);
        assertEquals(2, profile.messages());
    }

    @Test
    void levelUpIsReportedExactlyOnce() {
        boolean sawLevelUp = false;
        // Level 1 needs 100 XP; at 15-25 XP per message that's 4-7 awards
        for (int i = 0; i < 10; i++) {
            OptionalInt result = service.recordMessage("g1", "u1");
            if (result.isPresent()) {
                assertEquals(1, result.getAsInt());
                sawLevelUp = true;
                break;
            }
            clock.advance(XpService.COOLDOWN_MS);
        }
        assertTrue(sawLevelUp, "expected a level-up within 10 awards");
        assertTrue(repository.find("g1", "u1").orElseThrow().xp() >= 100);
    }

    @Test
    void cooldownsAreIndependentPerUser() {
        service.recordMessage("g1", "u1");
        service.recordMessage("g1", "u2");

        assertTrue(repository.find("g1", "u1").orElseThrow().xp() > 0);
        assertTrue(repository.find("g1", "u2").orElseThrow().xp() > 0);
    }
}
