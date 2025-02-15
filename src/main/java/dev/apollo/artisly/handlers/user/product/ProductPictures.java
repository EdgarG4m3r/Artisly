package dev.apollo.artisly.handlers.user.product;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.ProductImage;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.UUID;

public class ProductPictures implements APIHandler {
    @Override
    public void handle(Context context) {
        InputFilter.validateUUID("product_id", ParamField.PATH, context);

        if (context.attribute("hasErrors") != null) {
            return;
        }

        UUID productId = UUID.fromString(context.pathParam("product_id"));

        List<ProductImage> productPictures = Artisly.instance.getMediaService().getProductImages(productId);

        JSONObject response = new JSONObject();
        JSONArray urlsArray = new JSONArray();
        for (ProductImage productImage : productPictures) {
            JSONObject imageObject = new JSONObject();
            imageObject.put("url", productImage.getUrl());
            imageObject.put("filename", productImage.getFileName());
            imageObject.put("is_thumbnail", productImage.isThumbnail());

            urlsArray.add(imageObject);
        }
        response.put("images", urlsArray);

        StandarizedResponses.success(
                context,
                "SUCCESS",
                "Berhasil mendapatkan gambar produk.",
                "product_pictures",
                response
        );

    }
}
