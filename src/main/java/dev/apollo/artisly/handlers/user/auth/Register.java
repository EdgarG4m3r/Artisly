package dev.apollo.artisly.handlers.user.auth;

import dev.apollo.artisly.exceptions.EmailTakenException;
import dev.apollo.artisly.exceptions.PhoneNumberTakenException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.response.ErrorContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Register implements APIHandler {

    @Override
    public void handle(Context context) {
        InputFilter.validateEmail("email", ParamField.FORM, context);
        InputFilter.validateName("first_name", ParamField.FORM, context);
        InputFilter.validateName("last_name", ParamField.FORM, context);
        InputFilter.validatePassword("password", ParamField.FORM, context);
        InputFilter.validatePassword("confirm_password", ParamField.FORM, context);
        InputFilter.validatePhoneNumber("phone_number", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String email = context.formParam("email");
        String firstName = context.formParam("first_name");
        String lastName = context.formParam("last_name");
        char[] password = context.formParam("password").toCharArray();
        char[] confirmPassword = context.formParam("confirm_password").toCharArray();
        String phoneNumber = context.formParam("phone_number");

        if (!Arrays.equals(password, confirmPassword)) {
            ErrorContainer errorContainer = new ErrorContainer("confirm_password", "Passwords do not match");
            if (context.attribute("errors") == null)
            {
                context.attribute("errors", new ArrayList<ErrorContainer>());
                context.attribute("hasErrors", true);
            }
            ArrayList<ErrorContainer> errors = context.attribute("errors");
            errors.add(errorContainer);
            StandarizedResponses.invalidParameter(context);
            return;
        }

        try
        {
            User user = UserService.register(email, password, firstName, lastName, phoneNumber);
            StandarizedResponses.success(
                    context,
                    "REGISTER_SUCCESS",
                    "Successfully registered",
                    "registered_user",
                    user.toJSON()
            );
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat registrasi. Silahkan coba lagi nanti");
        } catch (EmailTakenException e) {
            StandarizedResponses.generalFailure(context, 400, "EMAIL_TAKEN_EXCEPTION", e.getMessage());
        } catch (PhoneNumberTakenException e) {
            StandarizedResponses.generalFailure(context, 400, "PHONE_TAKEN_EXCEPTION", e.getMessage());
        }
    }
}
