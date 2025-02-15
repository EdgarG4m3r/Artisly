package dev.apollo.artisly.handlers.user.order;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.datalayer.CRUDSOrderRecord;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateString("view", ParamField.QUERY, context, new String[]{"user", "store"});
        InputFilter.validateInt("page", ParamField.QUERY, context, 1, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }


        String view = context.queryParam("view");
        int page = Integer.parseInt(context.queryParam("page"));
        int limit = Integer.parseInt(context.queryParam("limit"));

        try
        {
            PaginatedOrder userOrders;
            if (view.equals("user")) {
                userOrders = OrderService.getOrders(userSessionContainer.getUser().id(), page, limit);
            }
            else
            {
                Optional<Store> store = StoreService.getStore(userSessionContainer.getUser().id());
                if (store.isEmpty()) {
                    StandarizedResponses.generalFailure(context, 404, "STORE_NOT_FOUND", "Tidak dapat menemukan toko.");
                    return;
                }
                userOrders = OrderService.getOrdersByStore(store.get().id(), page, limit);
            }
            JSONArray ordersArray = new JSONArray();
            for (Order order : userOrders.orders())
            {
                ImmutableAddress immutableAddress = AddressService.readImmutableAddress(order.immuteableAddressId()).get();
                ImmutableProduct immutableProduct = ProductService.readImmutableProduct(order.immuteableProductId()).get();
                User buyer = UserService.show(order.userId());
                Store store = StoreService.getStoreById(order.storeId()).get();

                JSONObject entry = new JSONObject();
                entry.put("order", order.toJSON());
                entry.put("address", immutableAddress.toJSON());
                entry.put("product", immutableProduct.toJSON());
                entry.put("buyer", buyer.toJSONRestricted());
                entry.put("store", store.toJSON());
                if (view.equals("user")) {
                    Optional<Review> review = ReviewService.getReviewByOrderId(order.id());
                    JSONObject reviewObject = new JSONObject();
                    if (review.isPresent()) {
                        reviewObject.put("data", review.get().toJSON());
                        reviewObject.put("reviewed", true);
                        reviewObject.put("can_review", false);
                    }
                    else
                    {
                        reviewObject.put("data", null);
                        reviewObject.put("reviewed", false);
                        reviewObject.put("can_review", canReview(order));
                    }
                    entry.put("review", reviewObject);
                }
                ordersArray.add(entry);
            }

            JSONObject response = new JSONObject();
            response.put("orders", ordersArray);
            response.put("page", userOrders.page());
            response.put("limit", userOrders.limit());
            response.put("total", userOrders.totalOrders());
            response.put("total_pages", userOrders.totalPages());
            response.put("ascending", userOrders.asc());
            response.put("sort_by", userOrders.column());

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil list order", "response", response);

        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal mengambil order, silahkan coba lagi nanti.");
            return;
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", "Pengguna tidak ditemukan.");
        }
    }

    public boolean canReview(Order order)
    {
        try
        {
            if (order.orderStatus() != OrderStatus.COMPLETED)
            {
                return false;
            }

            List<OrderRecord> orderRecords = OrderService.getOrderRecords(order.id());
            LocalDate completedDate = orderRecords.get(orderRecords.size() - 1).date();

            if (LocalDate.now().isAfter(completedDate.plusDays(30))) {
                return false;
            }

            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

}