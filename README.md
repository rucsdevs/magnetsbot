# Star

A Discord engagement bot for the Rutgers CS community. Members earn XP for
participating in chat, level up on a quadratic curve, and compete on a
per-server leaderboard. State is persisted in SQLite, the bot ships as a
Docker image, and every push to `main` is tested and deployed to AWS EC2
through GitHub Actions.

## Commands

| Command | Description |
| --- | --- |
| `/rank [member]` | Level, total XP, progress to the next level, and server rank |
| `/leaderboard` | Top 10 most active members of the server |
| `/ping` | Health check with gateway latency |

XP is awarded per message (15–25 XP, random) with a 60-second per-user
cooldown to prevent farming. Reaching level `n` requires `100·n²` total XP,
and level-ups are announced in the channel.

## Architecture

```
                      ┌─────────────────────────────────────────┐
                      │                Star                   │
 Discord Gateway ───► │  XpListener ──► XpService ──► XpRepo ──►│──► SQLite
   (JDA, websocket)   │                    ▲                    │   (/data volume)
                      │  CommandRegistry ──┘                    │
                      │   ├── /ping                             │
                      │   ├── /rank                             │
                      │   └── /leaderboard                      │
                      └─────────────────────────────────────────┘
```

- **`com.star.config`** — configuration from environment variables, with a
  `config.properties` fallback for local development. No secrets in the image
  or the repo.
- **`com.star.command`** — a small slash-command framework: each command is
  a self-contained class implementing `SlashCommand`, and `CommandRegistry`
  routes interactions by name. Adding a command is one class plus one line in
  `Star.java`.
- **`com.star.xp`** — the XP domain: leveling math (`XpMath`), business
  rules like cooldowns and level-up detection (`XpService`), and persistence
  (`XpRepository`). JDA dispatches events from a thread pool, so repository
  access is synchronized over the single SQLite connection.
- **`com.star.db`** — SQLite bootstrap and schema. Profiles are keyed by
  `(guild_id, user_id)` with an index on `(guild_id, xp DESC)` so leaderboard
  queries stay fast.

## Development

Requires JDK 21 and Maven.

```bash
cp config.example.properties config.properties   # add your bot token
mvn test                                          # run the test suite
mvn package && java -jar target/star.jar        # build and run
```

The XP engine is fully unit-tested (deterministic via injected `Clock` and
seeded `Random`) against an in-memory SQLite database — see `src/test`.

## Deployment

CI runs the test suite and a Docker build on every push and pull request.
Pushes to `main` additionally trigger a deployment: the image is built in CI,
shipped to an EC2 `t4g.micro` over SSH, and restarted with the XP database on
a persistent volume.

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for full setup instructions
(Discord application, EC2 provisioning, GitHub secrets).

## License

MIT — see [LICENSE](LICENSE).
