package dev.apollo.artisly.handlers.user.store;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.json.simple.JSONObject;

import java.util.UUID;

public class StoreBanner implements APIHandler {

    @Override
    public void handle(Context context) {
        InputFilter.validateUUID("store_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            return;
        }

        UUID storeId = UUID.fromString(context.pathParam("store_id"));

        String url = Artisly.instance.getMediaService().getStoreBannerUrl(storeId);

        JSONObject response = new JSONObject();
        response.put("url", url);

        StandarizedResponses.success(
                context,
                "SUCCESS",
                "Berhasil mendapatkan banner toko.",
                "store_banner",
                response
        );

    }
}
