package dev.apollo.artisly.models;


import org.json.simple.JSONObject;

import java.util.UUID;

/**
 * @param id         The ID of the order
 *
 * @param storeId         The ID of the store where the order was made
 * @param immuteableProductId   A copy of the product that was ordered
 * @param quantity The quantity of the product that was ordered
 * @param price      The price of the order
 * @param immuteableAddressId   A copy of the address where the order was sent
 * @param orderStatus A list of order records
 */

public record Order(UUID id, UUID storeId, UUID userId, UUID immuteableProductId, int quantity, double price, UUID immuteableAddressId, OrderStatus orderStatus) {

    public JSONObject toJSON()
    {
        JSONObject orderJson = new JSONObject();
        orderJson.put("id", id);
        orderJson.put("store_id", storeId);
        orderJson.put("user_id", userId);
        orderJson.put("product_id", immuteableProductId);
        orderJson.put("quantity", quantity);
        orderJson.put("price", String.format("%.2f", price));
        orderJson.put("address_id", immuteableAddressId);
        orderJson.put("order_status", orderStatus.toString());
        return orderJson;
    }

}
