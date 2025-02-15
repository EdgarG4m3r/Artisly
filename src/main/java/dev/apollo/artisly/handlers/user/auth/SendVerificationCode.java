package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.exceptions.EmailAlreadyVerified;
import dev.apollo.artisly.exceptions.RateLimitedException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import java.sql.SQLException;

public class SendVerificationCode implements APIHandler {


    @Override
    public void handle(Context context) {
        InputFilter.validateEmail("email", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.formParam("email");

        try
        {
            UserService.requestVerificationCode(email);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengirim kode verifikasi. Silahkan cek email anda.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        } catch (EmailAlreadyVerified e) {
            StandarizedResponses.generalFailure(context, 409, "EMAIL_ALREADY_VERIFIED", e.getMessage());
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengirim kode verifikasi, silahkan hubungi support kami atau coba lagi nanti");
        } catch (RateLimitedException e) {
            StandarizedResponses.generalFailure(context, 429, "RATE_LIMITED_EXCEPTION", e.getMessage());
        }
    }
}
