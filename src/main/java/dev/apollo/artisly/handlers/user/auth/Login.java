package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.EmailNotVerifiedException;
import dev.apollo.artisly.exceptions.InvalidCredentialsException;
import dev.apollo.artisly.exceptions.UserBannedException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import dev.apollo.artisly.session.exception.TokenSigningException;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class Login implements APIHandler {
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
        InputFilter.validatePassword("password", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.formParam("email");
        char[] password = context.formParam("password").toCharArray();

        try {
            String Ip;
            if (context.header("X-Forwarded-For") != null) {
                String ipRegex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
                Ip = context.header("X-Forwarded-For");
                if (!Ip.matches(ipRegex)) {
                    StandarizedResponses.invalidParameter(context);
                    return;
                }
            } else {
                Ip = context.ip();
            }
            UserSessionContainer userSession = UserService.login(Ip, email, password);
            String session = userSession.getToken();
            User user = userSession.getUser();

            JSONObject result = new JSONObject();
            result.put("token", session);
            result.put("user", user.toJSON());

            StandarizedResponses.success(
                    context,
                    "SUCCESS",
                    "Berhasil login",
                    "authenticated_user",
                    result
            );



        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        } catch (TokenSigningException e) {
            StandarizedResponses.generalFailure(context, 500, "TOKEN_SIGNING_EXCEPTION", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data pengguna, silahkan hubungi support kami atau coba lagi nanti");
        } catch (InvalidCredentialsException e) {
            StandarizedResponses.generalFailure(context, 401, "INVALID_CREDENTIALS_EXCEPTION", e.getMessage());
        } catch (UserBannedException e) {
            StandarizedResponses.generalFailure(context, 403, "USER_BANNED_EXCEPTION", e.getMessage());
        } catch (EmailNotVerifiedException e) {
            StandarizedResponses.generalFailure(context, 403, "EMAIL_NOT_VERIFIED_EXCEPTION", e.getMessage());
        }
    }
}
