package dev.apollo.artisly.handlers.admin.storereport;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.datalayer.CRUDSStoreReport;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.StoreReport;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.EmailService;
import dev.apollo.artisly.services.ReportService;
import dev.apollo.artisly.services.StoreService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Edit implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateUUID("report_id", ParamField.PATH, context);
        InputFilter.validateBoolean("is_resolved", ParamField.FORM, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID reportId = UUID.fromString(context.pathParam("report_id"));
        boolean isResolved = Boolean.parseBoolean(context.formParam("is_resolved"));

        try
        {
            ReportService.processReport(reportId, isResolved);
            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengubah status report");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengubah status report, silahkan hubungi support kami atau coba lagi nanti");
        }
        catch (UserNotFoundException e)
        {
            StandarizedResponses.generalFailure(context, 404, "USER_NOT_FOUND_EXCEPTION", e.getMessage());
        }
        catch (StoreNotExist e)
        {
            StandarizedResponses.generalFailure(context, 404, "STORE_NOT_FOUND_EXCEPTION", e.getMessage());
        }
    }
}
