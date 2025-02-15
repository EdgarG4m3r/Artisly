package dev.apollo.artisly.routes;

import io.javalin.Javalin;

public class RoutePATCH {

    public static void registerRoute(Javalin web) {
        //address
        web.patch("/address/{address_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Edit().handle(ctx);
        });

        //cart
        web.patch("/cart", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Modify().handle(ctx);
        });

        //store
        web.patch("/store", ctx -> {
            new dev.apollo.artisly.handlers.user.store.Edit().handle(ctx);
        });

        //edit order
        web.patch("/order/{order_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.order.Edit().handle(ctx);
        });

        //edit product
        web.patch("/product/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.Edit().handle(ctx);
        });

        web.patch("/admin/category/{category_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.category.Edit().handle(ctx);
        });

        web.patch("/admin/storereport/{report_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.storereport.Edit().handle(ctx);
        });

        web.patch("/admin/storevet/{vet_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.storevet.Edit().handle(ctx);
        });

        web.patch("/admin/user/{user_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.user.Edit().handle(ctx);
        });
    }
}
