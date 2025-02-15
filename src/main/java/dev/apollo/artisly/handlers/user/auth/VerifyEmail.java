package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.exceptions.EmailAlreadyVerified;
import dev.apollo.artisly.exceptions.InvalidVerificationCodeException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;

public class VerifyEmail implements APIHandler {
    @Override
    public void handle(Context context) {
        InputFilter.validateEmail("email", ParamField.QUERY, context);
        InputFilter.validateVerificationCode("code", ParamField.QUERY, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.queryParam("email");
        String code = context.queryParam("code");

        try {
            boolean result = UserService.verifyEmail(email, code);
            if (result)
            {
                StandarizedResponses.success(
                        context,
                        "EMAIL_VERIFIED",
                        "Email verified, Silahkan login kembali"
                );
            }
            else
            {
                StandarizedResponses.success(
                        context,
                        "LOGIN_VERIFIED",
                        "IP berhasil diverifikasi, silahkan login kembali"
                );
            }
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        } catch (EmailAlreadyVerified e) {
            StandarizedResponses.generalFailure(context, 409, "EMAIL_ALREADY_VERIFIED", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengirim kode verifikasi, silahkan hubungi support kami atau coba lagi nanti");
        } catch (InvalidVerificationCodeException e) {
            StandarizedResponses.generalFailure(context, 401, "INVALID_VERIFICATION_CODE_EXCEPTION", e.getMessage());
        }
    }
}
