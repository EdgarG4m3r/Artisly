package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
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
import java.util.List;

public class RequestPasswordReset implements APIHandler {
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

        InputFilter.validateEmail("email", ParamField.FORM, context);
        InputFilter.validatePhoneNumber("phone_number", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.formParam("email");
        String phoneNumber = context.formParam("phone_number");

        try
        {
            UserService.requestPasswordReset(email, phoneNumber);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengirimkan permintaan reset password, silahkan cek email anda!");
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data anda, silahkan coba lagi nanti!");
        } catch (AccountNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "ACCOUNT_NOT_FOUND_EXCEPTION", "Tidak dapat menemukan akun anda, silahkan cek kembali email & nomor telpon yang dimasukan");
        }
    }
}
