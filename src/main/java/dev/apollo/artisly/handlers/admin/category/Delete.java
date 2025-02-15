package dev.apollo.artisly.handlers.admin.category;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.CategoryNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.CategoryService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Delete implements APIHandler {
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
            boolean result = CategoryService.deleteCategory(categoryId);
            if (result)
            {
                StandarizedResponses.success(context, "SUCCESS", "Berhasil menghapus kategori");
            }
            else
            {
                StandarizedResponses.generalFailure(context, 400, "FAILED", "Gagal menghapus kategori");
            }
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat menghapus kategori");
        } catch (CategoryNotExist e) {
            StandarizedResponses.generalFailure(context, 500, "CATEGORY_NOT_EXIST", e.getMessage());
        }

    }
}
