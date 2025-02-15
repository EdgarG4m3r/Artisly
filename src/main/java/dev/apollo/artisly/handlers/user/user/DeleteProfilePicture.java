package dev.apollo.artisly.handlers.user.user;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.MediaService;
import io.javalin.http.Context;

public class DeleteProfilePicture implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        User user = userSessionContainer.getUser();

        MediaService mediaService = Artisly.instance.getMediaService();
        if (!mediaService.hasProfilePicture(user.id()))
        {
            StandarizedResponses.generalFailure(context, 404, "NO_PROFILE_PICTURE", "Pengguna tidak memiliki foto profil");
            return;
        }

        Artisly.instance.getMediaService().deleteProfilePicture(user.id());
        StandarizedResponses.success(context, "SUCCESS", "Berhasil menghapus foto profil pengguna.");
    }
}
