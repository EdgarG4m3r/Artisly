package dev.apollo.artisly.handlers.user.wishlist;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.managers.WishlistManager;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import io.javalin.http.Context;

public class Clear implements APIHandler {
    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        WishlistManager.clearWishlist(userSessionContainer.getUser().id());
        StandarizedResponses.success(context, "SUCCESS", "Berhasil membersihkan wishlist.");
    }
}
