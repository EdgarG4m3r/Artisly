package dev.apollo.artisly.handlers.user.storereport;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.ReportService;
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

        //UUID reportedStore, UUID reportedBy, Optional<UUID> reportedProduct, String reportReason
        InputFilter.validateUUID("store_id", ParamField.FORM, context);
        if (context.formParam("product_id") != null) {
            InputFilter.validateUUID("product_id", ParamField.FORM, context);
        }

        InputFilter.validateString("report_reason", ParamField.FORM, context, 500);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        UUID reportedStore = UUID.fromString(context.formParam("store_id"));
        UUID reportedBy = userSessionContainer.getUser().id();
        Optional<UUID> reportedProduct = Optional.empty();
        if (context.formParam("product_id") != null) {
            reportedProduct = Optional.of(UUID.fromString(context.formParam("product_id")));
        }
        String reportReason = context.formParam("report_reason");

        try
        {
            ReportService.createReport(reportedStore, reportedBy, reportedProduct, reportReason);
            if (reportedProduct.isPresent()) {
                StandarizedResponses.success(context, "SUCCESS", "Berhasil melaporkan produk.");
            }
            else
            {
                StandarizedResponses.success(context, "SUCCESS", "Berhasil melaporkan toko.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal melaporkan toko. Silahkan coba lagi.");
        } catch (ProductNotExist e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 404, "PRODUCT_NOT_FOUND", e.getMessage());
        }
    }
}
