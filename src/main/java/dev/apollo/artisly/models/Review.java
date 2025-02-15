package dev.apollo.artisly.models;


import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * @param id      The ID of the review
 * @param orderId       The ID of the order
 * @param rating  The rating of the review
 * @param date    The date the review was created
 * @param content The content of the review
 */

public record Review(UUID id, UUID orderId, int rating, LocalDate date, String content) {

    public JSONObject toJSON()
    {
        JSONObject object = new JSONObject();
        object.put("id", id.toString());
        object.put("order_id", orderId.toString());
        object.put("rating", rating);
        object.put("date", date.toString());
        object.put("content", content);
        return object;
    }

}
