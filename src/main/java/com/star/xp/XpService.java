package com.star.xp;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

/**
 * Business rules for XP: each qualifying message earns a random 15-25 XP,
 * with a per-user cooldown so rapid-fire messages don't farm XP.
 */
public final class XpService {

    static final int MIN_AWARD = 15;
    static final int MAX_AWARD = 25;
    static final long COOLDOWN_MS = 60_000;

    private final XpRepository repository;
    private final Clock clock;
    private final Random random;

    public XpService(XpRepository repository) {
        this(repository, Clock.systemUTC(), new Random());
    }

    /** Test constructor with injectable time and randomness. */
    XpService(XpRepository repository, Clock clock, Random random) {
        this.repository = repository;
        this.clock = clock;
        this.random = random;
    }

    /**
     * Awards XP for a message if the user is off cooldown.
     *
     * @return the new level if this award caused a level-up, otherwise empty
     */
    public OptionalInt recordMessage(String guildId, String userId) {
        long now = clock.millis();
        XpProfile profile = repository.find(guildId, userId)
                .orElseGet(() -> XpProfile.empty(guildId, userId));

        if (now - profile.lastAwardMs() < COOLDOWN_MS) {
            return OptionalInt.empty();
        }

        int award = MIN_AWARD + random.nextInt(MAX_AWARD - MIN_AWARD + 1);
        int levelBefore = profile.level();

        XpProfile updated = new XpProfile(
                guildId, userId, profile.xp() + award, profile.messages() + 1, now);
        repository.save(updated);

        int levelAfter = updated.level();
        return levelAfter > levelBefore ? OptionalInt.of(levelAfter) : OptionalInt.empty();
    }

    public Optional<XpProfile> profile(String guildId, String userId) {
        return repository.find(guildId, userId);
    }

    public int rank(String guildId, String userId) {
        return repository.rank(guildId, userId);
    }

    public List<XpProfile> leaderboard(String guildId, int limit) {
        return repository.topByGuild(guildId, limit);
    }
}
