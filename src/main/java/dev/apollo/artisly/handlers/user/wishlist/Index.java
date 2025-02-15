package dev.apollo.artisly.handlers.user.wishlist;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.managers.WishlistManager;
import dev.apollo.artisly.models.Cart;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.models.Wishlist;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.CartService;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        Wishlist wishlist = WishlistManager.getWishlist(userSessionContainer.getUser().id());
        JSONObject wishlistJson = new JSONObject();
        wishlistJson.put("items_in_wishlist", wishlist.productId().size());
        StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan wishlist.", "wishlist", wishlistJson);
    }
}
