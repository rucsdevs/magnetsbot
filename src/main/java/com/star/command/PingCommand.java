package com.star.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Health check: replies with the gateway round-trip latency. */
public final class PingCommand implements SlashCommand {

    @Override
    public SlashCommandData data() {
        return Commands.slash("ping", "Check that Star is alive and measure latency");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! Gateway latency: **%d ms**".formatted(gatewayPing)).queue();
    }
}
