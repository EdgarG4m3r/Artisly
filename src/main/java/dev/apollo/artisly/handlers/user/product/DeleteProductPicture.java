package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.MediaService;
import dev.apollo.artisly.services.ProductService;
import dev.apollo.artisly.services.StoreService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class DeleteProductPicture implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("product_id", ParamField.PATH, context);
        if (!context.pathParam("image_id").equalsIgnoreCase("thumbnail")) {
            InputFilter.validateUUID("image_id", ParamField.PATH, context);
        }

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        try
        {
            Optional<Store> storeOptional = StoreService.getStore(userSessionContainer.getUser().id());

            if (!storeOptional.isPresent())
            {
                StandarizedResponses.generalFailure(context, 404, "STORE_NOT_EXIST", "Toko tidak ditemukan.");
                return;
            }

            Optional<Product> productOptional = ProductService.getProduct(UUID.fromString(context.pathParam("product_id")));

            if (!productOptional.isPresent())
            {
                StandarizedResponses.generalFailure(context, 404, "PRODUCT_NOT_EXIST", "Produk tidak ditemukan.");
                return;
            }

            if (!productOptional.get().storeId().equals(storeOptional.get().id()))
            {
                StandarizedResponses.generalFailure(context, 403, "PRODUCT_NOT_EXIST", "Produk tidak ditemukan.");
                return;
            }

            UUID productId = UUID.fromString(context.pathParam("product_id"));
            String imageId = context.pathParam("image_id");

            MediaService mediaService = Artisly.instance.getMediaService();
            mediaService.deleteProductImage(productId, imageId);

            StandarizedResponses.success(context, "SUCCESS", "Berhasil menghapus gambar produk.");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat menghapus gambar produk. Silahkan coba lagi.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 500, "USER_NOT_FOUND_EXCEPTION", "Terjadi kesalahan saat menghapus gambar produk. Silahkan coba lagi.");
        }
    }
}
