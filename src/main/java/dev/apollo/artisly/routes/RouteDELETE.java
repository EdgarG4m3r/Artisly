package dev.apollo.artisly.routes;

import dev.apollo.artisly.handlers.user.auth.Logout;
import dev.apollo.artisly.handlers.user.user.DeleteProfilePicture;
import io.javalin.Javalin;

public class RouteDELETE {

    public static void registerRoute(Javalin web) {
        web.delete("/logout", ctx -> { new Logout().handle(ctx); });
        web.delete("/profilepicture", ctx -> new DeleteProfilePicture().handle(ctx));

        web.delete("/address/{address_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.address.Delete().handle(ctx);
        });

        //cart delete item
        web.delete("/cart/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Remove().handle(ctx);
        });

        //wishlist
        web.delete("/wishlist/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.wishlist.Remove().handle(ctx);
        });

        //cart delete all
        web.delete("/cart", ctx -> {
            new dev.apollo.artisly.handlers.user.cart.Clear().handle(ctx);
        });

        //wishlist delete all
        web.delete("/wishlist", ctx -> {
            new dev.apollo.artisly.handlers.user.wishlist.Clear().handle(ctx);
        });

        //delete product
        web.delete("/product/{product_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.Delete().handle(ctx);
        });

        //delete product image
        web.delete("/product/{product_id}/{image_id}", ctx -> {
            new dev.apollo.artisly.handlers.user.product.DeleteProductPicture().handle(ctx);
        });

        web.delete("/admin/category/{category_id}", ctx -> {
            new dev.apollo.artisly.handlers.admin.category.Delete().handle(ctx);
        });
    }
}
