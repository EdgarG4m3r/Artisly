package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.UserService;
import dev.apollo.artisly.session.exception.InvalidTokenException;
import dev.apollo.artisly.session.exception.MissingTokenException;
import dev.apollo.artisly.session.exception.UnsignedTokenException;
import io.javalin.http.Context;

public class Logout implements APIHandler {
    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        String token = userSessionContainer.getToken();

        try
        {
            UserService.logout(token);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil logout");
        } catch (InvalidTokenException e) {
            StandarizedResponses.generalFailure(context, 500, "INVALID_TOKEN_EXCEPTION", "Terjadi kesalahan saat logout, silahkan hubungi support kami atau coba lagi nanti");
        } catch (UnsignedTokenException e) {
            StandarizedResponses.generalFailure(context, 500, "UNSIGNED_TOKEN_EXCEPTION", "Terjadi kesalahan saat logout, silahkan hubungi support kami atau coba lagi nanti");
        } catch (MissingTokenException e) {
            StandarizedResponses.generalFailure(context, 500, "MISSING_TOKEN_EXCEPTION", "Terjadi kesalahan saat logout, silahkan hubungi support kami atau coba lagi nanti");
        }

    }
}
