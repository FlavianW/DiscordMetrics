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

    public static List<DiscordUser> getAllUsers(DiscordApi api) {
        List<DiscordUser> seisoCordUsers = new ArrayList<>();

        for (Server server : api.getServers()) {
            System.out.println("🔄 Téléchargement des membres du serveur : " + server.getName());

            Set<User> allUsers = ConcurrentHashMap.newKeySet();
            CompletableFuture<Void> done = new CompletableFuture<>();

            // Listener
            var listener = api.addServerMembersChunkListener((ServerMembersChunkEvent event) -> {
                if (!event.getServer().equals(server)) return;
                allUsers.addAll(event.getMembers());

                if (allUsers.size() >= event.getServer().getMemberCount()) {
                    done.complete(null);
                }
            });

            // Request to get all members to discord
            server.requestMembersChunks();

            try {
                // Wait 15 seconds max
                done.get(15, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("⚠️ Timeout ou erreur lors de la récupération des membres du serveur " + server.getName());
            }

            // Remove listener
            listener.remove();

            System.out.println("✅ " + allUsers.size() + " membres reçus pour " + server.getName());

            for (User user : allUsers) {
                String userPfp = "https://cdn.discordapp.com/avatars/" + user.getIdAsString() + "/" + user.getAvatarHash().orElse("") + "?size=2048";
                DiscordUser userObject = new DiscordUser(user.getIdAsString(), user.getName(), userPfp);
                System.out.println(userObject.getPfp());
                seisoCordUsers.add(userObject);
            }
        }

        return seisoCordUsers;
    }
}
