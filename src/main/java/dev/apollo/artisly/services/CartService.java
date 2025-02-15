package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.exceptions.BasketProductInsertionFailed;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.models.Cart;
import dev.apollo.artisly.models.Product;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CartService {

    private static final int CART_EXPIRATION_DAYS = 30;

    public static void modifyProductInCart(UUID userId, String productId, int quantity) throws ProductNotExist, SQLException, BasketProductInsertionFailed {

        Optional<Product> productOptional = ProductService.getProduct(UUID.fromString(productId));
        if (productOptional.isEmpty()) {
            throw new ProductNotExist("Produk tidak ditemukan.");
        }

        int stock = productOptional.get().stock();

        if (quantity > stock) {
            throw new BasketProductInsertionFailed("Stok tidak mencukupi. Stok tersedia: " + stock);
        }

        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String cartKey = "cart::" + userId.toString();
            if (quantity < 1) {
                jedis.hdel(cartKey, productId);
            } else {
                jedis.hset(cartKey, productId, String.valueOf(quantity));
            }
            jedis.expire(cartKey, (int) TimeUnit.DAYS.toSeconds(CART_EXPIRATION_DAYS));
        }




    }

    public static void removeProductFromCart(UUID userId, String productId) {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String cartKey = "cart::" + userId.toString();
            jedis.hdel(cartKey, productId);
            jedis.expire(cartKey, (int) TimeUnit.DAYS.toSeconds(CART_EXPIRATION_DAYS));
        }
    }

    public static void clearCart(UUID userId) {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String cartKey = "cart::" + userId.toString();
            jedis.del(cartKey);
        }
    }

    public static void addProductToCart(UUID userId, String productId, int quantity) throws SQLException, ProductNotExist, BasketProductInsertionFailed {
        Optional<Product> productOptional = ProductService.getProduct(UUID.fromString(productId));
        if (productOptional.isEmpty()) {
            throw new ProductNotExist("Produk tidak ditemukan.");
        }

        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            String cartKey = "cart::" + userId.toString();
            int stock = productOptional.get().stock();
            if (jedis.hexists(cartKey, productId)) {
                int currentQuantity = Integer.parseInt(jedis.hget(cartKey, productId));
                modifyProductInCart(userId, productId, currentQuantity + quantity);
            }
            else
            {
                if (quantity > stock) {
                    throw new BasketProductInsertionFailed("Pesanan melebihi stok. Stok tersisa: " + stock);
                }
                jedis.hset(cartKey, productId, String.valueOf(quantity));
            }
            jedis.expire(cartKey, (int) TimeUnit.DAYS.toSeconds(CART_EXPIRATION_DAYS));
            return;
        }
    }

    public static Cart getCart(UUID userId)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String cartKey = "cart::" + userId.toString();
            Map<String, String> cartData = jedis.hgetAll(cartKey);
            Map<UUID, Integer> result = new HashMap<>();

            for (Map.Entry<String, String> entry : cartData.entrySet()) {
                UUID productId = UUID.fromString(entry.getKey());
                int quantity = Integer.parseInt(entry.getValue());
                result.put(productId, quantity);
            }

            jedis.expire(cartKey, (int) TimeUnit.DAYS.toSeconds(CART_EXPIRATION_DAYS));
            Cart cart = new Cart(userId, result);
            return cart;
        }
    }

}
