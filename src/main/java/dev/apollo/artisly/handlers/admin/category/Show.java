package dev.apollo.artisly.handlers.admin.category;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Category;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.CategoryService;
import io.javalin.http.Context;

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

        InputFilter.validateUUID("category_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID categoryId = UUID.fromString(context.pathParam("category_id"));

        try
        {
            Optional<Category> categoryOptional = CategoryService.getCategory(categoryId);
            if (!categoryOptional.isPresent()) {
                StandarizedResponses.generalFailure(context, 404, "CATEGORY_NOT_FOUND", "Kategori tidak ditemukan");
                return;
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data kategori", "data", categoryOptional.get().toJSON());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Kesalahan saat mengambil data kategori");
        }
    }
}
