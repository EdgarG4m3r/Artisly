package dev.apollo.artisly.handlers.user.discussion;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.DiscussionService;
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

        InputFilter.validateUUID("product_id", ParamField.PATH, context);
        InputFilter.validateString("content", ParamField.FORM, context, 500);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID productId = UUID.fromString(context.pathParam("product_id"));
        String content = context.formParam("content");

        try
        {
            DiscussionService.postDiscussionReply(userSessionContainer.getUser().id(), productId, content);
            StandarizedResponses.success(context, "SUCCESS", "Diskusi berhasil diposting.");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat memposting diskusi. Silahkan coba lagi.");
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 400, "STORE_NOT_EXIST", e.getMessage());
        } catch (ProductNotExist e) {
            StandarizedResponses.generalFailure(context, 400, "PRODUCT_NOT_EXIST", e.getMessage());
        }
    }
}
