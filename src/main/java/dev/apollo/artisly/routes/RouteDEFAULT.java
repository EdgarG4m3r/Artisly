package dev.apollo.artisly.routes;

import dev.apollo.artisly.response.StandarizedResponses;
import io.javalin.Javalin;

public class RouteDEFAULT {

    public static void registerRoute(Javalin web) {
        web.get("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.post("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.delete("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.patch("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.put("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.options("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
        web.head("*", ctx -> {
            StandarizedResponses.generalFailure(ctx, 404, "NOT_FOUND", "Periksa kembali URL yang Anda masukkan!");
        });
    }
}
