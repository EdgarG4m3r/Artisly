package dev.apollo.artisly.handlers.user.review;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidOrderException;
import dev.apollo.artisly.exceptions.ReviewExpired;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.ReviewService;
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

        InputFilter.validateUUID("product_id", ParamField.FORM, context);
        InputFilter.validateUUID("order_id", ParamField.FORM, context);
        InputFilter.validateString("review", ParamField.FORM, context, 255);
        InputFilter.validateInt("rating", ParamField.FORM, context, 1, 5);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID orderID = UUID.fromString(context.formParam("order_id"));
        String review = context.formParam("review");
        int rating = Integer.parseInt(context.formParam("rating"));

        try {
            ReviewService.create(orderID, rating, review);
            StandarizedResponses.success(context, "SUCCESS", "Review berhasil dibuat");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat membuat review. Silahkan coba lagi.");
        } catch (InvalidOrderException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ORDER_EXCEPTION", e.getMessage());
        } catch (ReviewExpired e) {
            StandarizedResponses.generalFailure(context, 400, "REVIEW_EXPIRED", e.getMessage());
        }


    }
}
