package dev.apollo.artisly.handlers.admin.storereport;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.ProductService;
import dev.apollo.artisly.services.ReportService;
import dev.apollo.artisly.services.StoreService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Index implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        try
        {
            List<StoreReport> storeReports = ReportService.getStoreReportsForAdmins();
            JSONArray storeReportArray = new JSONArray();
            for (StoreReport storeReport : storeReports)
            {
                JSONObject reportEntry = new JSONObject();
                reportEntry.put("report", storeReport.toJSON());
                Store store;
                try
                {
                    Optional<Store> storeOptional = StoreService.getStoreById(storeReport.storeId());
                    if (storeOptional.isEmpty()) {
                        continue;
                    }
                    store = storeOptional.get();
                }
                catch (Exception e)
                {
                    continue;
                }
                reportEntry.put("store", store.toJSON());
                if (!storeReport.immuteableProductId().isEmpty())
                {
                    try {
                        Optional<ImmutableProduct> immutableProductOptional = ProductService.readImmutableProduct(storeReport.immuteableProductId().get());
                        if (immutableProductOptional.isEmpty()) {
                            reportEntry.put("product", null);
                        }
                        else
                        {
                            reportEntry.put("product", immutableProductOptional.get().toJSON());
                        }
                    }
                    catch (Exception e)
                    {
                        reportEntry.put("product", null);
                    }
                }

                try
                {
                    User user = UserService.show(storeReport.userId());
                    reportEntry.put("user", user.toJSON());
                }
                catch (UserNotFoundException e)
                {
                    reportEntry.put("user", null);
                }

                storeReportArray.add(reportEntry);
            }

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data report", "response", storeReportArray);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan pada server.");
        }
    }
}
