package dev.apollo.artisly.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.services.CategoryService;
import dev.apollo.artisly.services.ReviewService;
import dev.apollo.artisly.services.StoreService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//public record Product(UUID id, UUID storeId, UUID categoryId, String name, String description, int stock, double price, LocalDate created, LocalDate updated, List<String> tags) {

public record Product(
        @JsonProperty("objectID") @JsonAlias({"id"}) UUID id,
        @JsonProperty("store_id") UUID storeId,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("stock") int stock,
        @JsonProperty("price") double price,
        @JsonProperty("created") LocalDate created,
        @JsonProperty("updated") LocalDate updated) {

    public JSONObject toJSON()
    {
        JSONObject object = new JSONObject();
        object.put("id", id.toString());
        object.put("store_id", storeId.toString());
        object.put("category_id", categoryId.toString());
        object.put("name", name);
        object.put("description", description);
        object.put("stock", stock);
        object.put("price", String.format("%.2f", price));
        object.put("created", created.toString());
        object.put("updated", updated.toString());
        JSONArray images = new JSONArray();
        for (ProductImage image : Artisly.instance.getMediaService().getProductImages(id))
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

    @JsonProperty("score")
    public long rank()
    {
        try
        {

            double averageRating = ReviewService.getAverageRating(id);
            int reviewCount = ReviewService.getRatingCount(id);

            boolean isVetted = StoreService.getStore(storeId).get().storeVetId().isPresent();

            double averageRatingWeight = 0.40;
            double reviewCountWeight = 0.50;
            double vettedBonusWeight = 0.10;

            double avgRatingFactor = averageRating * averageRatingWeight;
            double reviewCountFactor = reviewCount * reviewCountWeight;

            double vettedBonus = isVetted ? vettedBonusWeight * (averageRating + reviewCount) : 0;

            double score = avgRatingFactor + reviewCountFactor + vettedBonus;

            return (long) score;


        }
        catch (Exception e)
        {
            return 0;
        }
    }

    @JsonProperty("images")
    public List<ProductImage> images()
    {
        return Artisly.instance.getMediaService().getProductImages(id);
    }

    @JsonProperty("image")
    public String thumbnail()
    {
        return Artisly.instance.getMediaService().getProductThumbnail(id).getUrl();
    }

    @JsonProperty("category")
    public String category()
    {
        try
        {
            return CategoryService.getCategory(categoryId).get().name();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Others";
        }
    }


    /*public List<String> tags()
    {
        //TODO: TOO SLOW, USE REDIS
        try
        {
            tags.add(CategoryService.getCategory(categoryId).get().name());
            tags.add(StoreService.getStore(storeId).get().name());
        }
        catch (Exception e)
        {
            //ignored
        }

        return tags;
    }*/

}
