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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.login.LoginException;
// this class is just for exception purposes; if the bots login attempt fails this'll come in handy.
//I dont need a lot of these imports, but they can be important for future reference. 

public class Bot extends ListenerAdapter{ //using listeneradapter class imported on line 5 as the superclass; listeneradapter is relevant since its needed for listening. listeneradapter also implements eventlistener which provides implementations for all various discord events
    public static void main(String[] args) throws LoginException, IOException{
        Properties properties = new Properties();
        try{ //error handling and importation of bot token located in a config properties file
            FileInputStream fis = new FileInputStream("config.properties");
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("Couldnt load config file");
            e.printStackTrace();
            return;
        }
        String token = properties.getProperty("BOT_TOKEN"); //hooking code up to bot and adding listening properties
        JDABuilder.createDefault(token).addEventListeners(new Bot()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event){ //code for configuring bot to respond to message when sent
        if (event.getAuthor().isBot()){ //to not interfere with other bots. 
            return;
        }
        else{
         String messageContent = event.getMessage().getContentRaw(); //finds message content from chat. 
         if (messageContent.equals("&ping")){
            event.getChannel().sendMessage("pong🤺").queue(); //the bot is constantly listening for messages; queue is used to not freeze bot
         }
        }

    }

} 
