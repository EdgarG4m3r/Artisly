package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.AccountNotVerifiedException;
import dev.apollo.artisly.exceptions.AlreadyHaveStoreException;
import dev.apollo.artisly.exceptions.EmailNotVerifiedException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.StoreService;
import io.javalin.http.Context;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class Create implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateString("store_name", ParamField.FORM, context, 50);
        InputFilter.validateString("store_note", ParamField.FORM, context, 500);
        MediaType[] allowedTypes = new MediaType[] {
                MediaType.image("png"),
                MediaType.image("jpeg"),
                MediaType.image("jpg"),
                MediaType.image("webp")
        };
        InputFilter.validateUploadedFiles("store_logo", 10 * 1024, allowedTypes, 10 * 1024, 1, context);
        InputFilter.validateUploadedFiles("store_banner", 10 * 1024, allowedTypes, 10 * 1024, 1, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String storeName = context.formParam("store_name");
        String storeNote = context.formParam("store_note");

        try
        {
            StoreService.createStore(userSessionContainer.getUser().id(), storeName, storeNote);
            Store store = StoreService.getStore(userSessionContainer.getUser().id()).get();
            try(InputStream is = context.uploadedFile("store_logo").content()) {
                Artisly.instance.getMediaService().uploadStoreLogo(store.id(), is, new Tika().detect(is));

            } catch (IOException e) {
                StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Terjadi kesalahan saat mengunggah gambar");
                return;
            }
            try(InputStream is = context.uploadedFile("store_banner").content()) {
                Artisly.instance.getMediaService().uploadStoreBanner(store.id(), is, new Tika().detect(is));
            } catch (IOException e) {
                StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Terjadi kesalahan saat mengunggah gambar");
                return;
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil membuat toko");
        } catch (AlreadyHaveStoreException e) {
            StandarizedResponses.generalFailure(context, 400, "ALREADY_HAVE_STORE_EXCEPTION", e.getMessage());
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", e.getMessage());
            e.printStackTrace();
        } catch (AccountNotVerifiedException e) {
            StandarizedResponses.generalFailure(context, 400, "ACCOUNT_NOT_VERIFIED_EXCEPTION", e.getMessage());
        } catch (EmailNotVerifiedException e) {
            StandarizedResponses.generalFailure(context, 400, "EMAIL_NOT_VERIFIED_EXCEPTION", e.getMessage());
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 400, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        }
    }
}
