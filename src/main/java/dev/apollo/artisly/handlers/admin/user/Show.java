package dev.apollo.artisly.handlers.admin.user;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Show implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("user_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID userId = UUID.fromString(context.pathParam("user_id"));

        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> userOptional = CRUDSUser.readById(connection, userId);

            if (userOptional.isEmpty())
            {
                throw new UserNotFoundException("Pengguna dengan ID " + userId + " tidak ditemukan");
            }

            User user = userOptional.get();

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan data pengguna", "user", user.toJSON());

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal mendapatkan data pengguna, silahkan cek stacktrace");
        }

        catch (UserNotFoundException e)
        {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        }

    }
}
