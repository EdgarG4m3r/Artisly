package dev.apollo.artisly.handlers.user.storevet;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.AlreadyHaveVetRequest;
import dev.apollo.artisly.exceptions.RequirementNotMet;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.StoreVetService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Create implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateUser(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateString("note", ParamField.FORM, context, 500);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String note = context.formParam("note");
        try {
            StoreVetService.requestVet(userSessionContainer.getUser().id(), note);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengirimkan permintaan vet.");
        } catch (AlreadyHaveVetRequest e) {
            StandarizedResponses.generalFailure(context, 400, "ALREADY_HAVE_VET_REQUEST", e.getMessage());
        }
        catch (SQLException e)
        {
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal mengirimkan permintaan vet. Silahkan coba lagi.");
            e.printStackTrace();
        } catch (StoreNotExist e) {
            StandarizedResponses.generalFailure(context, 400, "STORE_NOT_EXIST", e.getMessage());
        } catch (RequirementNotMet e) {
            StandarizedResponses.generalFailure(context, 400, "REQUIREMENT_NOT_MET", e.getMessage());
        }


    }
}
