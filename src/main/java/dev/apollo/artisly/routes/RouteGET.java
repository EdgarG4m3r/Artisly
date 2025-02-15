package dev.apollo.artisly.routes;

import dev.apollo.artisly.handlers.admin.user.Index;
import dev.apollo.artisly.handlers.user.auth.VerifyEmail;
import dev.apollo.artisly.handlers.user.user.Profile;
import dev.apollo.artisly.handlers.user.user.ProfilePicture;
import io.javalin.Javalin;

public class RouteGET {

    public static void registerRoute(Javalin web) {
        web.get("/ping", ctx -> {
            ctx.result("pong");
        });

        web.get("/profile", ctx -> {
            new Profile().handle(ctx);
        });

        web.get("/profilepicture/{user_id}", ctx -> {
            new ProfilePicture().handle(ctx);
        });

        web.get("/verify", ctx -> {
            new VerifyEmail().handle(ctx);
        });

        //store
        web.get("/store", ctx -> {
            new dev.apollo.artisly.handlers.user.store.ShowOwnStore().handle(ctx);
        });
        web.get("/store/{store_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.store.Show().handle(ctx);
        });

        //store banner & logo
        web.get("/storebanner/{store_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.store.StoreBanner().handle(ctx);
        });

        web.get("/storelogo/{store_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.store.StoreLogo().handle(ctx);
        });

        //address
        web.get("/address", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Index().handle(ctx);
        });

        web.get("/address/{address_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Show().handle(ctx);
        });



        //city
        web.get("/cities", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Cities().handle(ctx);
        });

        //category
        web.get("/category", ctx -> {
            new dev.apollo.artisly.handlers.user.category.Index().handle(ctx);
        });

        //cart index
        web.get("/cart", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Index().handle(ctx);
        });

        //cart show
        web.get("/cart/items", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Show().handle(ctx);
        });

        //wishlist index
        web.get("/wishlist", ctx -> {
            new dev.apollo.artisly.handlers.user.wishlist.Index().handle(ctx);
        });

        //wishlist show
        web.get("/wishlist/items", ctx -> {
            new dev.apollo.artisly.handlers.user.wishlist.Show().handle(ctx);
        });

        //discussion
        web.get("/discussion/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.discussion.Index().handle(ctx);
        });

        //show order
        web.get("/order/{order_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.order.Show().handle(ctx);
        });

        //index order
        web.get("/order", ctx -> {
            new dev.apollo.artisly.handlers.user.order.Index().handle(ctx);
        });

        //show product
        web.get("/product/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.Show().handle(ctx);
        });

        //show product picture
        web.get("/productpicture/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.ProductPictures().handle(ctx);
        });

        //index review
        web.get("/review/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.review.Index().handle(ctx);
        });

        //index product
        web.get("/product", ctx -> {
            new dev.apollo.artisly.handlers.user.product.Index().handle(ctx);
        });

        //index priority product
        web.get("/priorityproduct", ctx -> {
            new dev.apollo.artisly.handlers.user.product.IndexPriority().handle(ctx);
        });

        //index product in store
        web.get("/productinstore/{store_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.IndexByStore().handle(ctx);
        });

        web.get("/admin/category", ctx -> {
            new dev.apollo.artisly.handlers.admin.category.Index().handle(ctx);
        });

        web.get("/admin/category/{category_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.category.Show().handle(ctx);
        });

        web.get("/admin/order", ctx -> {
            new dev.apollo.artisly.handlers.admin.order.Index().handle(ctx);
        });

        web.get("/admin/order/{order_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.order.Show().handle(ctx);
        });

        web.get("/admin/storereport", ctx -> {
            new dev.apollo.artisly.handlers.admin.storereport.Index().handle(ctx);
        });

        web.get("/admin/storevet", ctx -> {
            new dev.apollo.artisly.handlers.admin.storevet.Index().handle(ctx);
        });

        web.get("/admin/user", ctx -> {
            new Index().handle(ctx);
        });

        web.get("/admin/user/{user_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.user.Show().handle(ctx);
        });

    }
}
