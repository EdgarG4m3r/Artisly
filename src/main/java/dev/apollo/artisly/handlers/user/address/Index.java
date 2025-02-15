package dev.apollo.artisly.handlers.user.address;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Address;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.AddressService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;

import java.sql.SQLException;
import java.util.List;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }
        try
        {
            List<Address> addresses = AddressService.readAllAddress(userSessionContainer.getUser().id());
            JSONArray addressesJson = new JSONArray();
            for (Address address : addresses) {
                addressesJson.add(address.toJSON());
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan alamat anda", "addresses", addressesJson);
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil alamat anda, silahkan hubungi support kami atau coba lagi nanti");
            return;
        }
    }
}
