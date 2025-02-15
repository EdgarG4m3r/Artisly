package dev.apollo.artisly.handlers.user.cart;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Cart;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.CartService;
import dev.apollo.artisly.services.ProductService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class Show implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        Cart cart = CartService.getCart(userSessionContainer.getUser().id());
        JSONObject cartJson = cart.toJSON();
        cartJson.put("items_in_cart", cart.productList().size());

        StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil keranjang", "cart", cartJson);

    }
}
