package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.pagination.PaginatedStore;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.StoreService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {

        InputFilter.validateInt("page", ParamField.QUERY, context, 10, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);
        InputFilter.validateString("sort_by", ParamField.QUERY, context, 255);
        InputFilter.validateBoolean("ascending", ParamField.QUERY, context);

        if (context.attribute("hasErrors") != null) {
            return;
        }

        int page = Integer.parseInt(context.queryParam("page"));
        int limit = Integer.parseInt(context.queryParam("limit"));
        String sortBy = context.queryParam("sort_by");
        boolean ascending = Boolean.parseBoolean(context.queryParam("ascending"));
        try
        {
            PaginatedStore paginatedStore = StoreService.getStores(page, limit, sortBy, ascending);
            JSONArray storesArray = new JSONArray();
            for (Store store : paginatedStore.stores())
            {
                JSONObject storeObject = new JSONObject();
                storeObject.put("id", store.id().toString());
                storeObject.put("name", store.name());
                storeObject.put("note", store.note());
                storeObject.put("created", store.created().toString());
                storeObject.put("store_vet_id", store.storeVetId().toString());
                try
                {
                    User user = UserService.show(store.userId());
                    JSONObject ownerObject = new JSONObject();
                    ownerObject.put("id", user.id().toString());
                    ownerObject.put("first_name", user.firstName());
                    ownerObject.put("last_name", user.lastName());
                    //ownerObject.put("email", user.email());
                    //ownerObject.put("phone_number", user.phoneNumber());
                    ownerObject.put("created", user.created().toString());
                    ownerObject.put("is_banned", user.banned());
                    ownerObject.put("email_verified", user.emailVerified().isPresent() ? user.emailVerified().get() : false);
                    storeObject.put("owner", ownerObject);


                } catch (UserNotFoundException e) {

                }

                storesArray.add(storeObject);

            }

            JSONObject response = new JSONObject();
            response.put("stores", storesArray);
            response.put("total_stores", paginatedStore.totalStores());
            response.put("page", paginatedStore.page());
            response.put("limit", paginatedStore.limit());
            response.put("total_pages", paginatedStore.totalPages());
            response.put("sort_by", paginatedStore.column());
            response.put("ascending", paginatedStore.asc());

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan daftar toko.", "stores", response);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat memuat daftar toko. Silahkan coba lagi nanti.");
        }

    }
}
