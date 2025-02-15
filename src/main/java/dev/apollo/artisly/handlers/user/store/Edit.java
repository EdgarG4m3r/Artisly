package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.AccountNotVerifiedException;
import dev.apollo.artisly.exceptions.EmailNotVerifiedException;
import dev.apollo.artisly.exceptions.StoreNotExist;
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

public class Edit implements APIHandler {

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
                MediaType.image("jpg")
        };

        if (context.uploadedFiles("store_logo").size() > 0)
            InputFilter.validateUploadedFiles("store_logo", 5 * 1024, allowedTypes, 5 * 1024, 1, context);
        if (context.uploadedFiles("store_banner").size() > 0)
            InputFilter.validateUploadedFiles("store_banner", 10 * 1024, allowedTypes, 10 * 1024, 1, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String storeName = context.formParam("store_name");
        String storeNote = context.formParam("store_note");

        try
        {
            StoreService.updateStore(userSessionContainer.getUser().id(), storeName, storeNote);
            Store store = StoreService.getStore(userSessionContainer.getUser().id()).get();
            System.out.println("STORE ID: " + store.id() + "STORE NAME: " + store.name() + "STORE LOGO SIZE: " + context.uploadedFiles("store_logo").size() + "STORE BANNER SIZE: " + context.uploadedFiles("store_banner").size());
            if (context.uploadedFiles("store_logo").size() > 0) {
                try(InputStream is = context.uploadedFile("store_logo").content()) {

                    String URL = Artisly.instance.getMediaService().uploadStoreLogo(store.id(), is, new Tika().detect(is));
                    System.out.println(URL);
                } catch (IOException e) {
                    StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Gagal mengunggah logo toko");
                    return;
                }
            }

            if (context.uploadedFiles("store_banner").size() > 0) {
                try(InputStream is = context.uploadedFile("store_banner").content()) {

                    String URL = Artisly.instance.getMediaService().uploadStoreBanner(store.id(), is, new Tika().detect(is));
                    System.out.println(URL);
                } catch (IOException e) {
                    StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Gagal mengunggah banner toko");
                    return;
                }
            }

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengedit toko");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengedit toko. Silahkan coba lagi nanti");
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 404, "STORE_NOT_EXIST", e.getMessage());
        } catch (AccountNotVerifiedException e) {
            StandarizedResponses.generalFailure(context, 403, "ACCOUNT_NOT_VERIFIED", e.getMessage());
        } catch (EmailNotVerifiedException e) {
            StandarizedResponses.generalFailure(context, 403, "EMAIL_NOT_VERIFIED_EXCEPTION", e.getMessage());
        }
        catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND", e.getMessage());
        }
    }
}
