package dev.apollo.artisly.handlers.user.user;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.util.UUID;

public class ProfilePicture implements APIHandler {

    @Override
    public void handle(Context context) {

        InputFilter.validateUUID("user_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID userID = UUID.fromString(context.pathParam("user_id"));

        String url = Artisly.instance.getMediaService().getProfilePictureUrl(userID);

        JSONObject response = new JSONObject();
        response.put("url", url);

        StandarizedResponses.success(
                context,
                "SUCCESS",
                "Berhasil mendapatkan gambar profil.",
                "profile_picture",
                response
        );

    }
}
