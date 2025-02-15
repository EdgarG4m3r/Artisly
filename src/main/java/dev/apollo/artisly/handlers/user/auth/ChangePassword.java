package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidCredentialsException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Arrays;

public class ChangePassword implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validatePassword("old_password", ParamField.FORM, context);
        InputFilter.validatePassword("new_password", ParamField.FORM, context);
        InputFilter.validatePassword("new_password_confirm", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        char[] oldPassword = context.formParam("old_password").toCharArray();
        char[] newPassword = context.formParam("new_password").toCharArray();
        char[] newPasswordConfirm = context.formParam("new_password_confirm").toCharArray();

        if (!Arrays.equals(newPassword, newPasswordConfirm)) {
            StandarizedResponses.generalFailure(context, 400, "NEW_PASSWORDS_DONT_MATCH", "Konfirmasi password baru tidak sesuai. Harap cek kembali");
            return;
        }

        if (Arrays.equals(oldPassword, newPassword)) {
            StandarizedResponses.generalFailure(context, 400, "NEW_PASSWORD_SAME_AS_OLD", "Password baru tidak boleh sama dengan password lama");
            return;
        }

        try
        {
            UserService.changePassword(userSessionContainer.getUser().id(), oldPassword, newPassword, userSessionContainer.token());
            StandarizedResponses.success(context, "SUCCESS", "Password anda berhasil diubah. Silahkan login kembali");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 400, "USER_NOT_FOUND", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengubah password anda, silahkan hubungi support kami atau coba lagi nanti");
        } catch (InvalidCredentialsException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_CREDENTIALS_EXCEPTION", e.getMessage());
        }
    }
}
