package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.ProductService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class  Delete implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("product_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID productId = UUID.fromString(context.pathParam("product_id"));

        try
        {
            boolean result = ProductService.deleteProductOfUser(userSessionContainer.getUser().id(), productId);
            if (!result) {
                throw new ProductNotExist("Produk tidak ditemukan.");
            }
            StandarizedResponses.success(context, "SUCCESS", "Produk berhasil dihapus.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 500, "USER_NOT_FOUND_EXCEPTION", "Terjadi kesalahan saat menghapus produk. Silahkan coba lagi.");
            return;
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat menghapus produk. Silahkan coba lagi.");
            return;
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 500, "STORE_NOT_EXIST", e.getMessage());
            return;
        } catch (ProductNotExist e) {
            StandarizedResponses.generalFailure(context, 500, "PRODUCT_NOT_EXIST", e.getMessage());
            return;
        }
    }
}
