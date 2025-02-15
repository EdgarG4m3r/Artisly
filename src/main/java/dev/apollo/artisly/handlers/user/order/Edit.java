package dev.apollo.artisly.handlers.user.order;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidOrderException;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.OrderStatus;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.OrderService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Edit implements APIHandler {


    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("order_id", ParamField.PATH, context);
        InputFilter.validateString("status", ParamField.QUERY, context, new String[]{"CANCELLED", "COMPLETED", "PROCESSED"});

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID orderId = UUID.fromString(context.pathParam("order_id"));
        OrderStatus status = OrderStatus.valueOf(context.queryParam("status"));

        try
        {
            switch (status)
            {
                case CANCELLED:
                    OrderService.cancelOrder(userSessionContainer.getUser().id(), orderId);
                    break;
                case COMPLETED:
                    OrderService.completeOrder(userSessionContainer.getUser().id(), orderId);
                    break;
                case PROCESSED:
                    OrderService.processOrder(userSessionContainer.getUser().id(), orderId);
                    break;
            }

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengubah status order.");
        } catch (InvalidOrderException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ORDER_EXCEPTION", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengubah status order. Silahkan coba lagi.");
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 400, "STORE_NOT_EXIST", e.getMessage());
        }

    }


}
