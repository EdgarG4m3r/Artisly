package dev.apollo.artisly.handlers.admin.category;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.CategoryService;
import io.javalin.http.Context;

import java.sql.SQLException;

public class Create implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateString("category_name", ParamField.FORM, context, 25);
        InputFilter.validateString("category_description", ParamField.FORM, context, 100);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String name = context.formParam("category_name");
        String description = context.formParam("category_description");

        try
        {
            CategoryService.createCategory(name, description);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil membuat kategori");
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Kesalahan saat membuat kategori");
        }

    }
}
