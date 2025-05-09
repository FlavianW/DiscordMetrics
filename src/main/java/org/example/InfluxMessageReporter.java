package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InfluxMessageReporter {

    private static void reportMessageTypes(TextChannel textChannel) {
        Map<String, Integer> textCount = new HashMap<>();
        Map<String, Integer> imageCount = new HashMap<>();

        try {
            textChannel.getMessagesAsStream().forEach(message -> {
                String username = message.getAuthor().getName();

                if (!message.getAttachments().isEmpty()) {
                    // Vérifie si c'est une image
                    boolean hasImage = message.getAttachments().stream()
                            .anyMatch(att -> att.isImage());
                    if (hasImage) {
                        imageCount.put(username, imageCount.getOrDefault(username, 0) + 1);
                    }
                }

                if (message.getContent() != null && !message.getContent().isBlank()) {
                    textCount.put(username, textCount.getOrDefault(username, 0) + 1);
                }
            });

            // Envoi à InfluxDB
            for (var entry : textCount.entrySet()) {
                sendDetailedMessageTypeToInfluxDB(((ServerChannel) textChannel).getName(), entry.getKey(), "text", entry.getValue());
            }
            for (var entry : imageCount.entrySet()) {
                sendDetailedMessageTypeToInfluxDB(((ServerChannel) textChannel).getName(), entry.getKey(), "image", entry.getValue());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la détection des types de messages dans " + ((ServerChannel) textChannel).getName());
        }
    }


    private static int getMessageCountForChannel(TextChannel channel) {
        try {
            long messageCount = channel.getMessagesAsStream().count();
            System.out.println("Messages in " + ((ServerChannel) channel).getName() + ": " + messageCount);
            return (int) messageCount;
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération des messages pour le canal " + ((ServerChannel) channel).getName());
            return 0;
        }
    }

    private static void sendDetailedMessageTypeToInfluxDB(String channel, String user, String type, int count) {
        user = user = user.substring(0, 1).toUpperCase() + user.substring(1);
        String body = String.format("message_type_count,channel=%s,user=%s,type=%s value=%d", channel, user, type, count);
        System.out.println(body);
        try {
            String influxDBUrl = "https://localhost:8086/api/v2/write?bucket=Discord_data&org=SeisoCord&precision=s";
            URL url = new URL(influxDBUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Token "+System.getenv("INFLUX_TOKEN"));
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);

            try (var os = connection.getOutputStream()) {
                os.write(body.getBytes("utf-8"));
            }

            if (connection.getResponseCode() != 204) {
                System.err.println("❌ Erreur InfluxDB pour " + user + " / " + type);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportAllChannels(DiscordApi api, Server server) {
        for (ServerChannel channel : server.getTextChannels()) {
            if (channel instanceof TextChannel textChannel) {
                reportMessageTypes(textChannel);
            }
        }
    }


}
