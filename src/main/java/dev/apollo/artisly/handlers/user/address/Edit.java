package dev.apollo.artisly.handlers.user.address;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.InvalidAddressException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.IndonesianCity;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.AddressService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Edit implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("address_id", ParamField.PATH, context);
        InputFilter.validateAddress("content", ParamField.FORM, context);
        InputFilter.validateCityName("city", ParamField.FORM, context);
        InputFilter.validatePhoneNumber("receiver_phone", ParamField.FORM, context);
        InputFilter.validateName("receiver_name", ParamField.FORM, context);
        InputFilter.validateAddressNotes("note", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID addressId = UUID.fromString(context.pathParam("address_id"));
        String content = context.formParam("content");
        IndonesianCity city = IndonesianCity.valueOf(context.formParam("city").toUpperCase().replaceAll(" ", "_"));
        String receiverPhone = context.formParam("receiver_phone");
        String receiverName = context.formParam("receiver_name");
        String note = context.formParam("note");

        try
        {
            if (AddressService.updateAddress(userSessionContainer.getUser().id(), addressId, receiverName, receiverPhone, content, note, city))
            {
                StandarizedResponses.success(context, "SUCCESS", "Alamat berhasil diperbarui");
            }
            else
            {
                StandarizedResponses.generalFailure(context, 400, "INVALID_ADDRESS_EXCEPTION", "Alamat yang ingin diperbarui tidak ditemukan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat memperbarui alamat, silahkan hubungi support kami atau coba lagi nanti");
            return;
        } catch (InvalidAddressException e) {
            StandarizedResponses.generalFailure(context, 400, "INVALID_ADDRESS_EXCEPTION", "Alamat yang ingin diperbarui tidak ditemukan");
            return;
        }
    }
}
