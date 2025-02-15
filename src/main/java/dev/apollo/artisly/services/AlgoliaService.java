package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.Product;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlgoliaService {

    public static void indexProduct(UUID productId)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            jedis.rpush("algolia-index-queue", productId.toString() + ":index");
        }
    }

    public static void deleteProduct(UUID productId)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            jedis.rpush("algolia-index-queue", productId.toString() + ":delete");
        }
    }

    public static void processProductQueue()
    {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            int itemIndexed = 0;

            List<Product> products = new ArrayList<>();
            List<String> productIds = new ArrayList<>();

            while (itemIndexed < 100) {
                String product = jedis.lpop("algolia-index-queue");
                if (product == null) {
                    break;
                }
                String[] parts = product.split(":");
                if (parts.length == 2) {
                    String productId = parts[0];
                    String action = parts[1];
                    if (action.equals("index")) {
                        Optional<Product> productToIndex = ProductService.getProduct(UUID.fromString(productId));
                        if (productToIndex.isPresent()) {
                            products.add(productToIndex.get());
                        }
                    } else if (action.equals("delete")) {
                        productIds.add(productId);
                    }
                }
                itemIndexed++;
            }

            if (products.size() > 0) {

            }

            if (productIds.size() > 0) {
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
