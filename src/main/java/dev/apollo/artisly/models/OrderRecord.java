package dev.apollo.artisly.models;

import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * @param id         The ID of the order
 * @param orderId         The ID of the order
 * @param orderStatus A list of order records
 * @param date      The date of the order record
 * @param note      The note of the order record
 */
public record OrderRecord (UUID id, UUID orderId, OrderStatus orderStatus, LocalDate date, Optional<String> note) {

    public JSONObject toJSON()
    {
        JSONObject orderRecordJson = new JSONObject();
        orderRecordJson.put("id", id);
        orderRecordJson.put("order_id", orderId);
        orderRecordJson.put("order_status", orderStatus.toString());
        orderRecordJson.put("date", date.toString());
        orderRecordJson.put("note", note.isPresent() ? note.get() : null);
        return orderRecordJson;
    }
}
