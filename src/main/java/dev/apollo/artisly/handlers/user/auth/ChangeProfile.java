package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.AccountNotVerifiedException;
import dev.apollo.artisly.exceptions.InvalidCredentialsException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

public class ChangeProfile implements APIHandler {


    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);

        if (userSessionContainer == null) {
            return;
        }

        User user = userSessionContainer.getUser();

        InputFilter.validatePassword("password", ParamField.FORM, context);
        InputFilter.validateName("first_name", ParamField.FORM, context);
        InputFilter.validateName("last_name", ParamField.FORM, context);
        InputFilter.validatePhoneNumber("phone_number", ParamField.FORM, context);
        if (context.formParam("no_ktp") != null)
        {
            InputFilter.validateKTP("no_ktp", ParamField.FORM, context);
        }

        if (context.uploadedFiles().size() > 0)
        {
            MediaType[] allowedTypes = new MediaType[] {
                    MediaType.parse("image/jpeg"),
                    MediaType.parse("image/png"),
                    MediaType.parse("image/jpg")
            };
            InputFilter.validateUploadedFiles("profile_picture", 1024, allowedTypes, 1024, 1, context);
        }
        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        try
        {
            if (context.uploadedFiles().size() > 0) {
                UploadedFile uploadedFile = context.uploadedFiles().get(0);
                try (InputStream inputStream = uploadedFile.content()) {
                    UserService.changeProfile(
                            user.id(),
                            context.formParam("password").toCharArray(),
                            Optional.ofNullable(context.formParam("first_name")),
                            Optional.ofNullable(context.formParam("last_name")),
                            Optional.ofNullable(context.formParam("phone_number")),
                            Optional.ofNullable(context.formParam("no_ktp")),
                            Optional.ofNullable(inputStream)
                    );
                } catch (AccountNotVerifiedException e) {
                    e.printStackTrace();
                    StandarizedResponses.internalError(context, e.getMessage());
                    return;
                }
            }
            else {
                UserService.changeProfile(
                        user.id(),
                        context.formParam("password").toCharArray(),
                        Optional.ofNullable(context.formParam("first_name")),
                        Optional.ofNullable(context.formParam("last_name")),
                        Optional.ofNullable(context.formParam("phone_number")),
                        Optional.ofNullable(context.formParam("no_ktp")),
                        Optional.empty());
            }

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengubah profil");
        }
        catch (SQLException e){
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi saat mengubah profil. Silahkan coba lagi");
            return;
        } catch (InvalidCredentialsException e) {
            StandarizedResponses.generalFailure(context, 401, "INVALID_CREDENTIALS_EXCEPTION", e.getMessage());
            return;
        } catch (AccountNotVerifiedException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 401, "ACCOUNT_NOT_VERIFIED_EXCEPTION", e.getMessage());
            return;
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
            return;
        } catch (IOException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "IO_EXCEPTION", "Terjadi saat mengubah profil. Silahkan coba lagi");
            return;
        }


    }
}
