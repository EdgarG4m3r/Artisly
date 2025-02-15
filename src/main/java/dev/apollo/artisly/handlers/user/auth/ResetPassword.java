package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidCredentialsException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import javax.security.auth.login.AccountNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ResetPassword implements APIHandler {
    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        context.attribute("hasErrors", null);
        List<ErrorContainer> errors = context.attribute("errors");
        errors.remove(0);
        if (userSessionContainer != null) {
            StandarizedResponses.methodNotAllowed(context, "You are already logged in, if you want to use a different account, please logout first");
            return;
        }

        InputFilter.validateUUID("user_id", ParamField.PATH, context);
        InputFilter.validateUUID("verification_token", ParamField.PATH, context);
        InputFilter.validatePassword("new_password", ParamField.FORM, context);

        InputFilter.validatePassword("confirm_password", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID userId = UUID.fromString(context.pathParam("user_id"));
        UUID verificationToken = UUID.fromString(context.pathParam("verification_token"));
        char[] password = context.formParam("new_password").toCharArray();
        char[] confirmPassword = context.formParam("confirm_password").toCharArray();

        if (Arrays.compare(password, confirmPassword) != 0) {
            StandarizedResponses.generalFailure(context, 400, "PASSWORD_MISMATCH", "Password tidak sama dengan konfirmasi password");
            return;
        }

        try
        {
            UserService.resetPassword(userId, verificationToken, password);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mereset password anda! Silahkan login dengan password baru!");
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data anda, silahkan coba lagi nanti!");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", "Tidak dapat menemukan akun anda, silahkan cek kembali link anda");
        } catch (InvalidCredentialsException e) {
            StandarizedResponses.generalFailure(context, 403, "INVALID_CREDENTIALS_EXCEPTION", "Kredensial salah, silahkan cek kembali link anda");
        }
    }
}
