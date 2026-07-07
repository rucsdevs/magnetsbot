package com.star.xp;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feeds guild messages into the {@link XpService} and announces level-ups.
 */
public final class XpListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(XpListener.class);

    private final XpService xpService;

    public XpListener(XpService xpService) {
        this.xpService = xpService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        xpService.recordMessage(guildId, userId).ifPresent(newLevel -> {
            log.debug("{} reached level {} in {}", userId, newLevel, guildId);
            event.getChannel()
                    .sendMessage("%s leveled up to **level %d**!"
                            .formatted(event.getAuthor().getAsMention(), newLevel))
                    .queue();
        });
    }
}
