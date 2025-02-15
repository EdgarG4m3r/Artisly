package dev.apollo.artisly.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.Wishlist;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WishlistManager {

    private static String getWishlistKey(UUID userId) {
        return "wishlist::" + userId.toString();
    }

    public static Wishlist getWishlist(UUID userId) {
        return new Wishlist(userId, getWishlistData(userId));
    }

    private static Set<UUID> getWishlistData(UUID userId) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            Set<String> wishlistData = jedis.smembers(getWishlistKey(userId));
            Set<UUID> result = new HashSet<>();

            for (String productId : wishlistData) {
                result.add(UUID.fromString(productId));
            }

            return result;
        }
    }

    public static void addProductToWishlist(UUID userId, UUID productId) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            jedis.sadd(getWishlistKey(userId), productId.toString());
        }
    }

    public static void removeProductFromWishlist(UUID userId, UUID productId) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            jedis.srem(getWishlistKey(userId), productId.toString());
        }
    }

    public static void clearWishlist(UUID userId) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            jedis.del(getWishlistKey(userId));
        }
    }
}
