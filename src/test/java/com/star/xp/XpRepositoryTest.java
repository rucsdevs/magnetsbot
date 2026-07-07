package com.star.xp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.star.db.Database;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XpRepositoryTest {

    private Database db;
    private XpRepository repository;

    @BeforeEach
    void setUp() {
        db = Database.inMemory();
        repository = new XpRepository(db);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void findReturnsEmptyForUnknownUser() {
        assertTrue(repository.find("g1", "unknown").isEmpty());
    }

    @Test
    void saveThenFindRoundTrips() {
        XpProfile profile = new XpProfile("g1", "u1", 250, 12, 1000);
        repository.save(profile);

        assertEquals(profile, repository.find("g1", "u1").orElseThrow());
    }

    @Test
    void saveUpsertsExistingProfile() {
        repository.save(new XpProfile("g1", "u1", 100, 5, 1000));
        repository.save(new XpProfile("g1", "u1", 120, 6, 2000));

        XpProfile stored = repository.find("g1", "u1").orElseThrow();
        assertEquals(120, stored.xp());
        assertEquals(6, stored.messages());
        assertEquals(2000, stored.lastAwardMs());
    }

    @Test
    void profilesAreScopedPerGuild() {
        repository.save(new XpProfile("g1", "u1", 500, 20, 0));
        repository.save(new XpProfile("g2", "u1", 100, 4, 0));

        assertEquals(500, repository.find("g1", "u1").orElseThrow().xp());
        assertEquals(100, repository.find("g2", "u1").orElseThrow().xp());
    }

    @Test
    void leaderboardOrdersByXpAndRespectsLimit() {
        repository.save(new XpProfile("g1", "low", 50, 2, 0));
        repository.save(new XpProfile("g1", "high", 900, 30, 0));
        repository.save(new XpProfile("g1", "mid", 400, 15, 0));
        repository.save(new XpProfile("g2", "other-guild", 9999, 99, 0));

        List<XpProfile> top = repository.topByGuild("g1", 2);
        assertEquals(List.of("high", "mid"), top.stream().map(XpProfile::userId).toList());
    }

    @Test
    void rankCountsUsersStrictlyAbove() {
        repository.save(new XpProfile("g1", "first", 900, 30, 0));
        repository.save(new XpProfile("g1", "second", 400, 15, 0));
        repository.save(new XpProfile("g1", "third", 50, 2, 0));

        assertEquals(1, repository.rank("g1", "first"));
        assertEquals(2, repository.rank("g1", "second"));
        assertEquals(3, repository.rank("g1", "third"));
    }
}
