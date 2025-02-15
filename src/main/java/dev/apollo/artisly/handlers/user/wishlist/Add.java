package dev.apollo.artisly.handlers.user.wishlist;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.managers.WishlistManager;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;

import java.util.UUID;

public class Add implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("product_id", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID productId = UUID.fromString(context.formParam("product_id"));

        WishlistManager.addProductToWishlist(userSessionContainer.getUser().id(), productId);
        StandarizedResponses.success(context, "SUCCESS", "Berhasil menambahkan produk ke wishlist.");
    }
}
