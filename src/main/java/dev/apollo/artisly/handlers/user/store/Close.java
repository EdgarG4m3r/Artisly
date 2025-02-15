package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.apache.tika.mime.MediaType;

public class Close implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }


    }
}
