package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;
import dev.apollo.artisly.models.pagination.PaginatedStore;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.security.auth.login.AccountNotFoundException;
import java.sql.SQLException;
import java.util.*;

public class ShowOwnStore implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        try
        {
            Optional<Store> optionalStore = StoreService.getStore(userSessionContainer.getUser().id());

            if (!optionalStore.isPresent())
            {
                StandarizedResponses.generalFailure(context, 404, "STORE_NOT_EXIST", "Toko tidak ditemukan.");
                return;
            }

            Store store = optionalStore.get();

            JSONObject response = new JSONObject();

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data toko.", "store", store.toJSON());
            return;

        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat memuat data toko. Silahkan coba lagi nanti.");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        }

    }
}
