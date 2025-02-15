package dev.apollo.artisly.handlers.user.category;

import dev.apollo.artisly.datalayer.CRUDSCategory;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Category;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.CategoryService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {

        try
        {
            JSONArray categories = new JSONArray();
            for (Category category : CategoryService.getCategories()) {
                JSONObject categoryJson = new JSONObject();
                categoryJson.put("id", category.id().toString());
                categoryJson.put("name", category.name());
                categoryJson.put("description", category.description());
                categories.add(categoryJson);
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil kategori", "categories", categories);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data kategori. Silahkan coba lagi nanti.");
        }

    }
}
