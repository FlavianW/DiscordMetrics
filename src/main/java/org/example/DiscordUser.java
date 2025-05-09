package org.example;

public class DiscordUser {

    private String id;
    private String username;
    private String pfp;

    public DiscordUser(String id, String username, String pfp) {
        this.id = id;
        this.username = username;
        this.pfp = pfp;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPfp() {
        return pfp;
    }
}

