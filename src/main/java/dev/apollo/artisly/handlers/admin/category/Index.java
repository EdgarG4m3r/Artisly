package dev.apollo.artisly.handlers.admin.category;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Category;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.CategoryService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class Index implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        try
        {
            JSONArray response = new JSONArray();
            List<Category> categoryList = CategoryService.getCategories();
            for (Category category : categoryList)
            {
                JSONObject categoryObject = new JSONObject();
                categoryObject.put("id", category.id());
                categoryObject.put("name", category.name());
                categoryObject.put("description", category.description());
                response.add(categoryObject);
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data kategori", "data", response);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Kesalahan saat mengambil data kategori");
        }

    }
}
