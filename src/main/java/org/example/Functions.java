package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMembersChunkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Functions {

    /**
     *
     * @param api DiscordApi
     * @param seisoCord SeisoCord server object
     * @return List of DiscordUser objects
     */
    public static List<DiscordUser> getAllUsers(DiscordApi api, Server seisoCord) {
        List<DiscordUser> seisoCordUsers = new ArrayList<>();


        Set<User> allUsers = ConcurrentHashMap.newKeySet();
        CompletableFuture<Void> done = new CompletableFuture<>();

        // Listener
        var listener = api.addServerMembersChunkListener((ServerMembersChunkEvent event) -> {
            if (!event.getServer().equals(seisoCord)) return;
            allUsers.addAll(event.getMembers());

            if (allUsers.size() >= event.getServer().getMemberCount()) {
                done.complete(null);
            }
        });

        // Request to get all members to discord
        seisoCord.requestMembersChunks();

        try {
            // Wait 15 seconds max
            done.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("⚠️ Timeout ou erreur lors de la récupération des membres du serveur " + seisoCord.getName());
        }

        // Remove listener
        listener.remove();


        for (User user : allUsers) {
            String userPfp = "https://cdn.discordapp.com/avatars/" + user.getIdAsString() + "/" + user.getAvatarHash().orElse("") + "?size=2048";
            DiscordUser userObject = new DiscordUser(user.getIdAsString(), user.getName(), userPfp);
            seisoCordUsers.add(userObject);
        }

        return seisoCordUsers;
    }
}
