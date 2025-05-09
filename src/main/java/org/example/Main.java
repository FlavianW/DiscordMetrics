package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMembersChunkEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder()
                .setToken(System.getenv("DISCORD_BOT_TOKEN"))
                .addIntents(Intent.MESSAGE_CONTENT,Intent.GUILD_MEMBERS).login().join();


        //Thread to get all users every 30 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("⏱ Rafraîchissement des utilisateurs à " + java.time.LocalTime.now());
            var users = Functions.getAllUsers(api);
            System.out.println("👥 Utilisateurs récupérés : " + users.size());
        }, 0, 20, TimeUnit.SECONDS); // Délai initial 0, puis toutes les 30 sec
    }
}
