package dev.apollo.artisly.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.UUID;

/**
 * @param userId    The ID of the user who added the product to their wishlist
 * @param productId The ID of the product added to the wishlist
 */

public record Wishlist(UUID userId, Set<UUID> productId) {

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", userId.toString());
        JSONArray jsonArray = new JSONArray();
        for (UUID entry : productId) {
            JSONObject product = new JSONObject();
            product.put("product_id", entry.toString());
            jsonArray.add(product);
        }
        jsonObject.put("products", jsonArray);
        return jsonObject;
    }

}
