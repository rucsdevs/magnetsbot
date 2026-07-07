package com.star.command;

import com.star.xp.XpProfile;
import com.star.xp.XpService;
import java.awt.Color;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Shows the top 10 members of the server by XP. */
public final class LeaderboardCommand implements SlashCommand {

    private static final int PAGE_SIZE = 10;
    private static final String[] MEDALS = {"\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49"};

    private final XpService xpService;

    public LeaderboardCommand(XpService xpService) {
        this.xpService = xpService;
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("leaderboard", "Show the most active members of this server")
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        List<XpProfile> top = xpService.leaderboard(event.getGuild().getId(), PAGE_SIZE);

        if (top.isEmpty()) {
            event.reply("Nobody has earned XP yet. Start chatting!").setEphemeral(true).queue();
            return;
        }

        StringBuilder body = new StringBuilder();
        for (int i = 0; i < top.size(); i++) {
            XpProfile p = top.get(i);
            String place = i < MEDALS.length ? MEDALS[i] : "**#" + (i + 1) + "**";
            body.append("%s <@%s> — level %d (%d XP)%n"
                    .formatted(place, p.userId(), p.level(), p.xp()));
        }

        var embed = new EmbedBuilder()
                .setTitle("Leaderboard — " + event.getGuild().getName())
                .setColor(new Color(0x5865F2))
                .setDescription(body.toString())
                .build();

        event.replyEmbeds(embed).queue();
    }
}
