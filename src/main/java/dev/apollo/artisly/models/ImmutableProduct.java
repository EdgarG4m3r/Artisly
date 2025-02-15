package dev.apollo.artisly.models;

import dev.apollo.artisly.Artisly;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record ImmutableProduct(UUID id, UUID productId, UUID storeId, UUID categoryId, String name, String description, double price, LocalDate created, LocalDate updated) {

    public JSONObject toJSON()
    {
        JSONObject object = new JSONObject();
        object.put("id", id.toString());
        object.put("product_id", productId.toString());
        object.put("store_id", storeId.toString());
        object.put("category_id", categoryId.toString());
        object.put("name", name);
        object.put("description", description);
        object.put("price", String.format("%.2f", price));
        object.put("created", created.toString());
        object.put("updated", updated.toString());
        JSONArray images = new JSONArray();
        for (ProductImage image : Artisly.instance.getMediaService().getImmutableProductImages(id))
        {
            images.add(image.toJSON());
            if (image.isThumbnail())
            {
                object.put("thumbnail", image.toJSON());
            }
        }
        object.put("images", images);
        return object;
    }
}
