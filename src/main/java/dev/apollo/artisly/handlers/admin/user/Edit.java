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
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class Edit implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("user_id", ParamField.PATH, context);
        InputFilter.validateString("user_first_name", ParamField.FORM, context, 50);
        InputFilter.validateString("user_last_name", ParamField.FORM, context, 50);
        InputFilter.validateBoolean("user_banned", ParamField.FORM, context);
        InputFilter.validateBoolean("user_admin", ParamField.FORM, context);


        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID userId = UUID.fromString(context.pathParam("user_id"));
        String firstName = context.formParam("user_first_name");
        String lastName = context.formParam("user_last_name");
        boolean banned = Boolean.parseBoolean(context.formParam("user_banned"));
        boolean admin = Boolean.parseBoolean(context.formParam("user_admin"));

        try
        {
            UserService.editUser(userId, firstName, lastName, banned, admin);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengubah user");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal saat mengubah user");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 400, "USER_NOT_FOUND", "User tidak ditemukan");
        }
    }
}
