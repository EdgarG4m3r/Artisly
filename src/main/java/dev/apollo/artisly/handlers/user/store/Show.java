package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import javax.security.auth.login.AccountNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

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

        InputFilter.validateUUID("store_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            return;
        }

        UUID storeId = UUID.fromString(context.pathParam("store_id"));

        try
        {
            Optional<Store> optionalStore = StoreService.getStoreById(storeId);

            if (!optionalStore.isPresent())
            {
                StandarizedResponses.generalFailure(context, 404, "STORE_NOT_FOUND", "Toko tidak ditemukan.");
                return;
            }

            Store store = optionalStore.get();

            if (isAuth) {
                if (store.userId().equals(userSessionContainer.getUser().id())) {
                    isOwner = true;
                }
            }

            User seller = UserService.show(store.userId());

            if (seller.banned())
            {
                StandarizedResponses.generalFailure(context, 403, "USER_BANNED", "Penjual telah diblokir.");
                return;
            }

            long totalProducts = ProductService.getProductsFromStore(storeId, "%", 1, 1, "product_created", true).totalProducts();
            long totalReviews = ReviewService.getRatingCountOfStore(storeId);
            double averageRating = ReviewService.getAverageRatingOfStore(storeId);

            JSONObject response = new JSONObject();
            response.put("id", store.id().toString());
            response.put("name", store.name());
            response.put("note", store.note());
            response.put("created", store.created().toString());
            JSONObject sellerObject = new JSONObject();
            sellerObject.put("id", seller.id().toString());
            sellerObject.put("first_name", seller.firstName());
            sellerObject.put("last_name", seller.lastName());
            response.put("seller", sellerObject);
            response.put("total_products", totalProducts);
            response.put("total_reviews", totalReviews);
            response.put("average_rating", averageRating);
            response.put("is_owner", isOwner);
            if (isOwner)
            {
                PaginatedOrder paginatedOrder = OrderService.getOrdersByStore(storeId, 1, Integer.MAX_VALUE);
                List<Order> allTimeOrders = paginatedOrder.orders();

                long totalOrders = allTimeOrders.size();
                long totalSales = 0;
                double totalRevenue = 0;
                for (Order order : allTimeOrders)
                {
                    totalSales += order.quantity();
                    totalRevenue += order.price();
                }

                long totalCompletedOrders = 0;
                for (Order order : allTimeOrders)
                {
                    if (order.orderStatus() == OrderStatus.COMPLETED)
                    {
                        totalCompletedOrders++;
                    }
                }

                long totalCancelledOrders = 0;
                for (Order order : allTimeOrders)
                {
                    if (order.orderStatus() == OrderStatus.CANCELLED)
                    {
                        totalCancelledOrders++;
                    }
                }

                long totalPendingOrders = 0;
                for (Order order : allTimeOrders)
                {
                    if (order.orderStatus() == OrderStatus.CREATED)
                    {
                        totalPendingOrders++;
                    }
                }

                long totalProcessingOrders = 0;
                for (Order order : allTimeOrders)
                {
                    if (order.orderStatus() == OrderStatus.PROCESSED)
                    {
                        totalProcessingOrders++;
                    }
                }

                HashMap<UUID, Integer> uniqueCustomers = new HashMap<>();
                for (Order order : allTimeOrders)
                {
                    if (uniqueCustomers.containsKey(order.userId()))
                    {
                        uniqueCustomers.put(order.userId(), uniqueCustomers.get(order.userId()) + 1);
                    }
                    else
                    {
                        uniqueCustomers.put(order.userId(), 1);
                    }
                }




                JSONObject storeStats = new JSONObject();
                storeStats.put("total_orders", totalOrders);
                storeStats.put("total_sales", totalSales);
                storeStats.put("total_revenue", totalRevenue);
                storeStats.put("total_completed_orders", totalCompletedOrders);
                storeStats.put("total_cancelled_orders", totalCancelledOrders);
                storeStats.put("total_pending_orders", totalPendingOrders);
                storeStats.put("total_processing_orders", totalProcessingOrders);

                storeStats.put("total_unique_customers", uniqueCustomers.size());
                int totalRepeatCustomers = 0;
                for (Map.Entry<UUID, Integer> entry : uniqueCustomers.entrySet())
                {
                    if (entry.getValue() > 1)
                    {
                        totalRepeatCustomers++;
                    }
                }
                storeStats.put("total_repeat_customers", totalRepeatCustomers);
                response.put("store_stats", storeStats);


            }
            response.put("is_verified", !store.storeVetId().isEmpty());

            StandarizedResponses.success(context, "SUCCESS", "Berhasil memuat data toko.", "store", response);

        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat memuat data toko. Coba lagi nanti.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND", e.getMessage());
        }

    }
}
