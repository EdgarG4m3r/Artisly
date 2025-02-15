package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.CategoryNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.ProductService;
import dev.apollo.artisly.services.StoreService;
import io.javalin.http.Context;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Create implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("category_id", ParamField.FORM, context);
        InputFilter.validateString("product_name", ParamField.FORM, context, 100);
        InputFilter.validateString("product_description", ParamField.FORM, context, 1000);
        InputFilter.validateDouble("product_price", ParamField.FORM, context, 1000, 100000000);
        InputFilter.validateInt("product_stock", ParamField.FORM, context, 1, 50000);

        MediaType[] allowedFilesExtension = new MediaType[] {
                MediaType.parse("image/jpeg"),
                MediaType.parse("image/png"),
                MediaType.parse("image/webp"),
                MediaType.parse("image/jpg")
        };

        InputFilter.validateUploadedFiles("product_thumbnail", 10240, allowedFilesExtension, 10240, 1, context);
        //InputFilter.validateUploadedFiles("product_images", 5 * 1024, allowedFilesExtension, 5 * 5 * 1024, 5, context);

        //if (context.uploadedFiles("product_images").size() > 0)
        //{
        //    InputFilter.validateUploadedFiles("product_images", 5 * 1024, allowedFilesExtension, 5 * 5 * 1024, 5, context);
        //}

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID categoryId = UUID.fromString(context.formParam("category_id"));
        String productName = context.formParam("product_name");
        String productDescription = context.formParam("product_description");
        double productPrice = Double.parseDouble(context.formParam("product_price"));
        int productStock = Integer.parseInt(context.formParam("product_stock"));

        try
        {
            Optional<Store> store = StoreService.getStore(userSessionContainer.getUser().id());
            if (store.isEmpty()) {
                StandarizedResponses.generalFailure(context, 500, "STORE_NOT_FOUND", "Anda belum memiliki toko. Silahkan buat toko terlebih dahulu.");
                return;
            }

            Product product = ProductService.createProduct(store.get().id(), categoryId, productName, productDescription, productPrice, productStock);

            if (context.uploadedFiles("product_thumbnail").size() > 0) {
                try(InputStream is = context.uploadedFile("product_thumbnail").content()) {
                    Artisly.instance.getMediaService().uploadProductImage(product.id(), is, new Tika().detect(is), true);
                } catch (IOException e) {
                    StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Terjadi kesalahan saat mengunggah gambar produk. Silahkan coba lagi.");
                }
            }

            /**if (context.uploadedFiles("product_images").size() > 0) {
                for (UploadedFile file : context.uploadedFiles("product_images")) {
                    try(InputStream is = file.content()) {
                        Artisly.instance.getMediaService().uploadProductImage(product.id(), is, new Tika().detect(is), false);
                    } catch (IOException e) {
                        StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Terjadi kesalahan saat mengunggah gambar produk. Silahkan coba lagi.");
                    }
                }
            }**/


            StandarizedResponses.success(context, "SUCCESS", "Produk berhasil dibuat.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 500, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat membuat produk. Silahkan coba lagi.");
        } catch (CategoryNotExist e) {
            StandarizedResponses.generalFailure(context, 500, "CATEGORY_NOT_EXIST", e.getMessage());
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 500, "STORE_NOT_FOUND", e.getMessage());
        }
    }

}
