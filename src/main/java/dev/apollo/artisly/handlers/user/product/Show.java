package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;
import dev.apollo.artisly.models.pagination.PaginatedReview;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Show implements APIHandler {
    @Override
    public void handle(Context context) {
        boolean isAuth = false;
        boolean isOwner = false;

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        context.attribute("hasErrors", null);
        if (context.attribute("errors") != null) {
            List<ErrorContainer> errors = context.attribute("errors");
            if (errors.size() > 0) {
                errors.remove(0);
            }
        }
        if (userSessionContainer != null) {
            isAuth = true;
        }

        InputFilter.validateUUID("product_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID productId = UUID.fromString(context.pathParam("product_id"));

        try
        {
            Optional<Product> productOptional = ProductService.getProduct(productId);

            if (!productOptional.isPresent())
            {
                StandarizedResponses.generalFailure(context, 404, "PRODUCT_NOT_EXIST", "Produk tidak ditemukan.");
                return;
            }

            Product product = productOptional.get();

            Store store = StoreService.getStoreById(product.storeId()).get();

            if (isAuth) {
                if (store.userId().equals(userSessionContainer.getUser().id())) {
                    isOwner = true;
                }
            }

            if (!isOwner)
            {
                try
                {
                    User seller = UserService.show(store.userId());
                    if (seller.banned())
                    {
                        StandarizedResponses.generalFailure(context, 403, "PRODUCT_NOT_EXIST", "Penjual ini telah diblokir.");
                        return;
                    }
                }
                catch (UserNotFoundException e)
                {
                    StandarizedResponses.generalFailure(context, 404, "PRODUCT_NOT_EXIST", "Penjual tidak ditemukan.");
                    return;
                }
            }

            JSONObject response = new JSONObject();
            response.put("store", store.toJSON());

            response.put("product", product.toJSON());
            response.put("is_owner", isOwner);

            int totalReviews = ReviewService.getRatingCount(productId);
            double averageRating = ReviewService.getAverageRating(productId);

            if (isOwner) {
                JSONObject productStats = new JSONObject();

                PaginatedOrder paginatedOrder = OrderService.getOrdersByProduct(productId, 1, Integer.MAX_VALUE, "product_created", true);
                List<Order> allTimeOrders = paginatedOrder.orders();

                long totalOrders = allTimeOrders.size();
                long totalSales = 0;
                double totalRevenue = 0;

                for (Order order : allTimeOrders) {
                    totalSales += order.quantity();
                    totalRevenue += order.price();
                }

                productStats.put("total_orders", totalOrders);
                productStats.put("total_sales", totalSales);
                productStats.put("total_revenue", totalRevenue);

                response.put("product_stats", productStats);

            }

            response.put("average_rating", averageRating);
            response.put("total_reviews", totalReviews);

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan produk.", "product", response);
            return;

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data produk. Silahkan coba lagi nanti.");
            return;
        }



    }
}
