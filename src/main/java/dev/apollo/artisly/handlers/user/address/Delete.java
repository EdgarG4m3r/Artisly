package dev.apollo.artisly.handlers.user.address;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidAddressException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.AddressService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Delete implements APIHandler {

    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("address_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID addressId = UUID.fromString(context.pathParam("address_id"));

        try {
            if (AddressService.deleteAddress(userSessionContainer.getUser().id(), addressId))
            {
                StandarizedResponses.success(context, "SUCCESS", "Alamat berhasil dihapus");
                return;
            }
            else
            {
                StandarizedResponses.generalFailure(context, 400, "INVALID_ADDRESS_EXCEPTION", "Alamat yang ingin anda hapus tidak ditemukan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat menghapus alamat anda, silahkan hubungi support kami atau coba lagi nanti");
            return;
        } catch (InvalidAddressException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ADDRESS_EXCEPTION", "Alamat yang ingin anda hapus tidak ditemukan");
            return;
        }


    }
}
