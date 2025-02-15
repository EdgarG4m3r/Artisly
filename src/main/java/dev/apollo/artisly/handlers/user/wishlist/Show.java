package dev.apollo.artisly.handlers.user.wishlist;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.managers.WishlistManager;
import dev.apollo.artisly.models.Cart;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.models.Wishlist;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.CartService;
import dev.apollo.artisly.services.ProductService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class Show implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        Wishlist wishlist = WishlistManager.getWishlist(userSessionContainer.getUser().id());
        JSONObject wishlistJson = wishlist.toJSON();
        wishlistJson.put("items_in_cart", wishlist.productId().size());
        List<Product> products = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(wishlist.productId().size());
        for (UUID productId : wishlist.productId()) {
            CompletableFuture.runAsync(() -> {
                try {
                    Optional<Product> product = ProductService.getProduct(productId);
                    if (product.isPresent()) {
                        products.add(product.get());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONArray productsJson = new JSONArray();
        for (Product product : products) {
            productsJson.add(product.toJSON());
        }
        wishlistJson.put("products", productsJson);

        StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan wishlist.", "wishlist", wishlistJson);

    }
}
