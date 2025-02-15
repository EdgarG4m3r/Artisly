package dev.apollo.artisly.models;


public record UserSessionContainer(String token, User user) {
    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

}
