package dev.apollo.artisly.handlers.user.address;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.AddressMaximumCountException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.IndonesianCity;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.AddressService;
import io.javalin.http.Context;

import java.sql.SQLException;

public class Create implements APIHandler {

    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateAddress("content", ParamField.FORM, context);
        InputFilter.validateCityName("city", ParamField.FORM, context);
        InputFilter.validatePhoneNumber("receiver_phone", ParamField.FORM, context);
        InputFilter.validateName("receiver_name", ParamField.FORM, context);
        InputFilter.validateAddressNotes("note", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String content = context.formParam("content");
        IndonesianCity city = IndonesianCity.valueOf(context.formParam("city").toUpperCase().replaceAll(" ", "_"));
        System.out.println(city);
        String receiverPhone = context.formParam("receiver_phone");
        String receiverName = context.formParam("receiver_name");
        String note = context.formParam("note");


        try
        {
            AddressService.createAddress(userSessionContainer.getUser().id(), receiverName, receiverPhone, content, note, city);
            StandarizedResponses.success(context, "SUCCESS", "Address created successfully");
        } catch (AddressMaximumCountException e) {
            StandarizedResponses.generalFailure(context, 400, "ADDRESS_MAXIMUM_COUNT_EXCEPTION", e.getMessage());
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "An error occurred while creating your address, please contact our support");
            return;
        }


    }
}
