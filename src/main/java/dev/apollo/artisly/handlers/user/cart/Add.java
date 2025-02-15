package dev.apollo.artisly.handlers.user.cart;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.BasketProductInsertionFailed;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.CartService;
import io.javalin.http.Context;

import java.sql.SQLException;

public class Add implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("product_id", ParamField.FORM, context);
        InputFilter.validateQuantity("quantity", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String productId = context.formParam("product_id");
        int quantity = Integer.parseInt(context.formParam("quantity"));

        try
        {
            CartService.addProductToCart(userSessionContainer.getUser().id(), productId, quantity);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil menambahkan produk ke dalam keranjang.");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat menambahkan produk ke dalam keranjang. Silahkan coba lagi.");
            return;
        }
        catch (ProductNotExist e)
        {
            StandarizedResponses.generalFailure(context, 500, "PRODUCT_NOT_EXIST", e.getMessage());
            return;
        } catch (BasketProductInsertionFailed e) {
            StandarizedResponses.generalFailure(context, 500, "BASKET_PRODUCT_INSERTION_FAILED", e.getMessage());
            return;
        }
    }
}
