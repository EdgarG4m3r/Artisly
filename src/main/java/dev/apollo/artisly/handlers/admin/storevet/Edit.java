package dev.apollo.artisly.handlers.admin.storevet;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.StoreVetService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.UUID;

public class Edit implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("vet_id", ParamField.PATH, context);
        InputFilter.validateBoolean("accepted", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID vetId = UUID.fromString(context.pathParam("vet_id"));
        boolean accepted = Boolean.parseBoolean(context.formParam("accepted"));

        try
        {
            if (accepted)
            {
                StoreVetService.acceptVetRequest(vetId);
            }
            else
            {
                StoreVetService.rejectVetRequest(vetId);
            }
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengubah status verifikasi");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal saat mengubah status verifikasi");
        } catch (StoreNotExist e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 404, "STORE_NOT_EXIST", "Toko tidak ditemukan");
        }
    }
}
