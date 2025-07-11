package com.RUCS.bot;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
/*
 * imported these classes for discord api interaction, bot setup, and event handling.
 * 2nd line import is to configure and build connection to discord
 * 3rd line is to set bot status
 * 4th line is to have the event object, which contains details about messages sent to be listened to
 * 5th line allows for listening
 * 6th line specifies permissions
 * 7th line governs how jda fetches and caches member lists (might not be necessary for this bot though)
 * 8th line works with the 7th line and defines which server members jda should keep in cache
 */
import okhttp3.internal.http2.Http2Connection.Listener;

import javax.security.auth.login.LoginException;
// this class is just for exception purposes; if the bots login attempt fails this'll come in handy.

public class Bot extends ListenerAdapter{ //using listeneradapter class imported on line 5 as the superclass; listeneradapter is relevant since its needed for listening. listeneradapter also implements eventlistener which provides implementations for all various discord events


} 
