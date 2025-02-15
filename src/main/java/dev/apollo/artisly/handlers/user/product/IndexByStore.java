package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.pagination.PaginatedProduct;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.ProductService;
import dev.apollo.artisly.services.ReviewService;
import dev.apollo.artisly.services.StoreService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class IndexByStore implements APIHandler {

    @Override
    public void handle(Context context) {
        InputFilter.validateUUID("store_id", ParamField.PATH, context);
        InputFilter.validateInt("page", ParamField.QUERY, context, 1, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);
        if (context.queryParam("search") != null)
            InputFilter.validateString("search", ParamField.QUERY, context, 255);
        InputFilter.validateString("sort_by", ParamField.QUERY, context, new String[]{"review", "price", "created"});
        InputFilter.validateBoolean("ascending", ParamField.QUERY, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID storeId = UUID.fromString(context.pathParam("store_id"));
        int page = Integer.parseInt(context.queryParam("page"));
        int limit = Integer.parseInt(context.queryParam("limit"));
        String query = context.queryParam("search") == null ? "%" : context.queryParam("search");
        String sortBy = context.queryParam("sort_by");
        boolean ascending = Boolean.parseBoolean(context.queryParam("ascending"));

        try {
            PaginatedProduct paginatedProduct = ProductService.getProductsFromStore(storeId, query, page, limit, sortBy, ascending);
            JSONArray productArray = new JSONArray();
            CountDownLatch latch = new CountDownLatch(paginatedProduct.products().size());

            for(Product product : paginatedProduct.products()) {

                CompletableFuture.runAsync(() -> {
                    try {
                        JSONObject productObject = product.toJSON();
                        String reviews = String.format("%.2f", ReviewService.getAverageRating(product.id()));
                        productObject.put("average_rating", reviews);
                        productObject.put("total_reviews", ReviewService.getRatingCount(product.id()));

                        try {
                            Store store = StoreService.getStoreById(product.storeId()).get();
                            productObject.put("store", store.toJSON());
                            productObject.put("is_priority", store.storeVetId().isEmpty() ? false : true);
                        } catch (NoSuchElementException e) {
                            Artisly.getLogger().error("User not found for store ID: " + product.storeId(), e);
                        }

                        productArray.add(productObject);
                    } catch (Exception e) {
                        Artisly.getLogger().error("Failed to get product data", e);
                    }

                    latch.countDown();
                });
            }

            latch.await();


            JSONObject response = new JSONObject();
            response.put("products", productArray);
            response.put("total_products", paginatedProduct.totalProducts());
            response.put("page", paginatedProduct.page());
            response.put("limit", paginatedProduct.limit());
            response.put("total_pages", paginatedProduct.totalPages());
            response.put("sort_by", paginatedProduct.column());
            response.put("ascending", paginatedProduct.asc());

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil produk", "products", response);
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil produk, silahkan hubungi support kami atau coba lagi nanti");
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "INTERRUPTED_EXCEPTION", "Terjadi kesalahan saat mengambil produk, silahkan hubungi support kami atau coba lagi nanti");
        }
    }
}
