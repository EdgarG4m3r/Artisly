package dev.apollo.artisly.handlers.admin.user;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.EmailTakenException;
import dev.apollo.artisly.exceptions.PhoneNumberTakenException;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.HashEngine;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.EmailService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class Create implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateEmail("user_email", ParamField.FORM, context);
        InputFilter.validateString("user_first_name", ParamField.FORM, context, 50);
        InputFilter.validateString("user_last_name", ParamField.FORM, context, 50);
        InputFilter.validatePhoneNumber("user_phone_number", ParamField.FORM, context);


        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.formParam("user_email");
        String firstName = context.formParam("user_first_name");
        String lastName = context.formParam("user_last_name");
        String phoneNumber = context.formParam("user_phone_number");

        try
        {
            UserService.registerAdmin(email, firstName, lastName, phoneNumber);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil membuat user");
        } catch (SQLException e) {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal saat membuat user");
        } catch (EmailTakenException e) {
            StandarizedResponses.generalFailure(context, 400, "EMAIL_TAKEN", "Email sudah terdaftar");
        } catch (PhoneNumberTakenException e) {
            StandarizedResponses.generalFailure(context, 400, "PHONE_NUMBER_TAKEN", "Nomor telepon sudah terdaftar");
        }


    }
}
