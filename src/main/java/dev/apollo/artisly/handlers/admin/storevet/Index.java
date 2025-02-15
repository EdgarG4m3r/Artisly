package dev.apollo.artisly.handlers.admin.storevet;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.StoreVet;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.StoreService;
import dev.apollo.artisly.services.StoreVetService;
import dev.apollo.artisly.services.UserService;
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
            List<StoreVet> storeVets = StoreVetService.getStoreVetRequestForAdmins();
            JSONArray storeVetArray = new JSONArray();
            for (StoreVet storeVet : storeVets)
            {
                try
                {
                    JSONObject storeVetEntry = new JSONObject();
                    storeVetEntry.put("vet", storeVet.toJSON());
                    User requester = UserService.show(storeVet.userId());
                    Store store = StoreService.getStoreById(storeVet.storeId()).get();
                    storeVetEntry.put("requester", requester.toJSON());
                    storeVetEntry.put("store", store.toJSON());

                    storeVetArray.add(storeVetEntry);
                }
                catch (Exception e)
                {
                    continue;
                }
            }

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data vet", "response", storeVetArray);
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan pada server.");
        }
    }
}
