package dev.apollo.artisly.routes;

import dev.apollo.artisly.handlers.user.auth.*;
import io.javalin.Javalin;

public class RoutePOST {

    public static void registerRoute(Javalin web) {
        web.post("/login", ctx -> {
            new Login().handle(ctx);
        });

        web.post("/register", ctx -> {
            new Register().handle(ctx);
        });

        web.post("/verify", ctx -> {
            new VerifyEmail().handle(ctx);
        });

        web.post("/verify/resend", ctx -> {
            new SendVerificationCode().handle(ctx);
        });

        web.post("/changepassword", ctx -> {
            new ChangePassword().handle(ctx);
        });

        web.post("/requestpasswordreset", ctx -> {
            new RequestPasswordReset().handle(ctx);
        });

        web.post("/resetpassword/{user_id}/{verification_token}", ctx -> {
            new ResetPassword().handle(ctx);
        });

        web.post("/profile", ctx -> {
            new ChangeProfile().handle(ctx);
        });

        //address
        web.post("/address", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Create().handle(ctx);
        });

        //cart
        web.post("/cart", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Add().handle(ctx);
        });

        //wishlist
        web.post("/wishlist", ctx -> {
            new dev.apollo.artisly.handlers.user.wishlist.Add().handle(ctx);
        });

        //store
        web.post("/store", ctx -> {
            new dev.apollo.artisly.handlers.user.store.Create().handle(ctx);
        });

        //store vet
        web.post("/storevet", ctx -> {
            new dev.apollo.artisly.handlers.user.storevet.Create().handle(ctx);
        });

        web.post("/storereport", ctx -> {
            new dev.apollo.artisly.handlers.user.storereport.Create().handle(ctx);
        });

        //discussion
        web.post("/discussion/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.discussion.Create().handle(ctx);
        });

        //create order
        web.post("/order", ctx -> {
            new dev.apollo.artisly.handlers.user.order.Create().handle(ctx);
        });

        //create product
        web.post("/product", ctx -> {
            new dev.apollo.artisly.handlers.user.product.Create().handle(ctx);
        });

        //create review
        web.post("/review/", ctx -> {
            new dev.apollo.artisly.handlers.user.review.Create().handle(ctx);
        });

        web.post("/admin/category", ctx -> {
            new dev.apollo.artisly.handlers.admin.category.Create().handle(ctx);
        });

        web.post("/admin/user", ctx -> {
            new dev.apollo.artisly.handlers.admin.user.Create().handle(ctx);
        });
    }
}
