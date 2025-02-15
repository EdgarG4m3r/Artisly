package dev.apollo.artisly.models;

import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.services.ProductService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @param userId    The ID of the user who added the product to their cart
 * @param productList The list of products added to the cart
 */
public record Cart(UUID userId, Map<UUID, Integer> productList) {

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", userId.toString());
        JSONArray jsonArray = new JSONArray();
        Map<Product, Integer> fullProductList = getFullProductList();
        for (Map.Entry<Product, Integer> entry : fullProductList.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            JSONObject productObject = product.toJSON();

            JSONObject entryObject = new JSONObject();
            entryObject.put("product", productObject);
            entryObject.put("quantity", quantity);
            jsonArray.add(entryObject);
        }
        jsonObject.put("products", jsonArray);
        double totalPrice = 0;
        for (Map.Entry<Product, Integer> entry : fullProductList.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += product.price() * quantity;
        }
             jsonObject.put("total_price", String.format("%.2f", totalPrice));
        return jsonObject;
    }

    public Map<Product, Integer> getFullProductList() {
        CountDownLatch latch = new CountDownLatch(productList.size());
        Map<Product, Integer> fullProductList = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : productList.entrySet()) {

            CompletableFuture.runAsync(() -> {
                try {
                    Optional<Product> product = ProductService.getProduct(entry.getKey());
                    if (product.isPresent()) {
                        fullProductList.put(product.get(), entry.getValue());
                    }
                    else
                    {
                        Product bogusProduct = new Product(
                                entry.getKey(),
                                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                                "DELETED",
                                "Produk ini telah dihapus oleh penjual",
                                0,
                                0,
                                LocalDate.now(),
                                LocalDate.now()
                        );
                        fullProductList.put(bogusProduct, entry.getValue());
                    }
                }
                catch (SQLException e)
                {
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

        return fullProductList;
    }

}
