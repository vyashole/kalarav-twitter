package com.adwaitvyas.kalarav;

/**
 * Holds Username and tweet ID
 */
public class TweetData {
    public String username;
    public long id;

    public TweetData(String username, long id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
