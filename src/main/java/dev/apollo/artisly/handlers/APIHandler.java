package dev.apollo.artisly.handlers;

import io.javalin.http.Context;

public interface APIHandler {
    void handle(Context context);
}
