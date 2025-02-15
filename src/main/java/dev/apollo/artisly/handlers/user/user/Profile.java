package dev.apollo.artisly.handlers.user.user;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Profile implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        User user = userSessionContainer.getUser();
        JSONObject userJSON = user.toJSON();
        StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan profil.", "user", userJSON);

    }
}
