package dev.apollo.artisly.handlers.user.order;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Show implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("order_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID orderId = UUID.fromString(context.pathParam("order_id"));

        try
        {
            Optional<Order> orderOptional = OrderService.getOrder(orderId);
            if (orderOptional.isEmpty()) {
                StandarizedResponses.generalFailure(context, 404, "INVALID_ORDER_EXCEPTION", "Tidak dapat menemukan order.");
                return;
            }

            Order order = orderOptional.get();
            Optional<Store> storeOptional = StoreService.getStoreById(order.storeId());


            if (order.userId() != userSessionContainer.getUser().id())
            {
                if (storeOptional.isEmpty()) {
                    StandarizedResponses.generalFailure(context, 404, "STORE_NOT_FOUND", "Tidak dapat menemukan toko.");
                    return;
                }
            }

            Optional<ImmutableAddress> optionalImmutableAddress = AddressService.readImmutableAddress(order.immuteableAddressId());
            Optional<ImmutableProduct> optionalImmutableProduct = ProductService.readImmutableProduct(order.immuteableProductId());
            User buyer = UserService.show(order.userId());

            JSONObject response = new JSONObject();

            JSONArray orderRecordsArray = new JSONArray();
            for (OrderRecord orderRecord : OrderService.getOrderRecords(order.id())) {
                orderRecordsArray.add(orderRecord.toJSON());
            }


            response.put("order", order.toJSON());
            response.put("buyer", buyer.toJSON());
            response.put("store", storeOptional.get().toJSON());
            response.put("immutable_address", optionalImmutableAddress.get().toJSON());
            response.put("immutable_product", optionalImmutableProduct.get().toJSON());
            response.put("order_records", orderRecordsArray);

            StandarizedResponses.success(context, "SUCCESS", "Order ditemukan.", "order", response);
            return;


        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data order. Silahkan coba lagi.");
            return;
        }catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", "Pengguna tidak ditemukan.");
            return;
        }
    }
}
