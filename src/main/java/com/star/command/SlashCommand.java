package com.star.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * A single slash command. Implementations declare their Discord-facing
 * definition and handle invocations; the {@link CommandRegistry} routes
 * events to them by name.
 */
public interface SlashCommand {

    /** Command definition registered with Discord (name, description, options). */
    SlashCommandData data();

    void execute(SlashCommandInteractionEvent event);
}
