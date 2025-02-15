package dev.apollo.artisly.handlers.user.cart;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.CartService;
import io.javalin.http.Context;

public class Clear implements APIHandler {
    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        CartService.clearCart(userSessionContainer.getUser().id());
        StandarizedResponses.success(context, "SUCCESS", "Berhasil menghapus semua produk dalam keranjang.");
    }
}
