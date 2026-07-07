package com.star.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes slash-command interactions to the matching {@link SlashCommand}.
 * Adding a command is a one-line change in {@link com.star.Star}.
 */
public final class CommandRegistry extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);

    private final Map<String, SlashCommand> commands = new LinkedHashMap<>();

    public CommandRegistry(SlashCommand... commands) {
        for (SlashCommand command : commands) {
            this.commands.put(command.data().getName(), command);
        }
    }

    /** Definitions for bulk registration with Discord on startup. */
    public List<SlashCommandData> commandData() {
        return commands.values().stream().map(SlashCommand::data).toList();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommand command = commands.get(event.getName());
        if (command == null) {
            return;
        }
        try {
            command.execute(event);
        } catch (RuntimeException e) {
            log.error("Command /{} failed", event.getName(), e);
            if (!event.isAcknowledged()) {
                event.reply("Something went wrong running that command.").setEphemeral(true).queue();
            }
        }
    }
}
