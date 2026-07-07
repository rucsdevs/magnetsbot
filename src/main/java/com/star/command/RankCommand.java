package com.star.command;

import com.star.xp.XpMath;
import com.star.xp.XpProfile;
import com.star.xp.XpService;
import java.awt.Color;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Shows a member's level, XP, progress to the next level, and server rank. */
public final class RankCommand implements SlashCommand {

    private final XpService xpService;

    public RankCommand(XpService xpService) {
        this.xpService = xpService;
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("rank", "Show your level, XP, and server rank")
                .addOption(OptionType.USER, "member", "Look up another member instead of yourself")
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User target = event.getOption("member") != null
                ? event.getOption("member").getAsUser()
                : event.getUser();

        String guildId = event.getGuild().getId();
        Optional<XpProfile> profile = xpService.profile(guildId, target.getId());

        if (profile.isEmpty()) {
            event.reply("%s hasn't earned any XP yet. Say something!".formatted(target.getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        XpProfile p = profile.get();
        int rank = xpService.rank(guildId, target.getId());
        long intoLevel = p.xp() - XpMath.xpForLevel(p.level());
        long levelSpan = XpMath.xpForLevel(p.level() + 1) - XpMath.xpForLevel(p.level());

        var embed = new EmbedBuilder()
                .setAuthor(target.getEffectiveName(), null, target.getEffectiveAvatarUrl())
                .setColor(new Color(0x5865F2))
                .addField("Level", String.valueOf(p.level()), true)
                .addField("Rank", "#" + rank, true)
                .addField("Total XP", String.valueOf(p.xp()), true)
                .addField("Progress",
                        "%d / %d XP to level %d".formatted(intoLevel, levelSpan, p.level() + 1), false)
                .build();

        event.replyEmbeds(embed).queue();
    }
}
