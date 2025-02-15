package dev.apollo.artisly.handlers.user.order;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidAddressException;
import dev.apollo.artisly.exceptions.InvalidOrderException;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Order;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.OrderService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Create implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("address_id", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID addressId = UUID.fromString(context.formParam("address_id"));

        try
        {
            OrderService.createOrder(userSessionContainer.getUser().id(), addressId);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil membuat order. Dalam beberapa saat, anda akan mendapatkan email konfirmasi.");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat membuat order. Silahkan coba lagi.");
        } catch (InvalidAddressException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ADDRESS_EXCEPTION", e.getMessage());
        } catch (InvalidOrderException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ORDER_EXCEPTION", e.getMessage());
        }

    }
}
