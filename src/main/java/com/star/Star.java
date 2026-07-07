package com.star;

import com.star.command.CommandRegistry;
import com.star.command.LeaderboardCommand;
import com.star.command.PingCommand;
import com.star.command.RankCommand;
import com.star.config.BotConfig;
import com.star.db.Database;
import com.star.xp.XpListener;
import com.star.xp.XpRepository;
import com.star.xp.XpService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point. Wires configuration, persistence, the XP engine, and slash
 * commands together, then hands control to JDA's event loop.
 */
public final class Star {
    private static final Logger log = LoggerFactory.getLogger(Star.class);

    public static void main(String[] args) throws InterruptedException {
        BotConfig config = BotConfig.load();

        Database database = Database.open(config.databasePath());
        XpRepository repository = new XpRepository(database);
        XpService xpService = new XpService(repository);

        CommandRegistry commands = new CommandRegistry(
                new PingCommand(),
                new RankCommand(xpService),
                new LeaderboardCommand(xpService));

        JDA jda = JDABuilder.createLight(config.token(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.watching("the leaderboard"))
                .addEventListeners(new XpListener(xpService), commands)
                .build();

        jda.updateCommands().addCommands(commands.commandData()).queue();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down");
            jda.shutdown();
            database.close();
        }));

        jda.awaitReady();
        log.info("Star is online in {} guild(s)", jda.getGuilds().size());
    }

    private Star() {
    }
}
