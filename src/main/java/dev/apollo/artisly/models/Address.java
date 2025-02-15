package dev.apollo.artisly.models;

import org.json.simple.JSONObject;

import java.util.UUID;

/**
 * @param id            The address ID
 * @param userId               The user ID
 * @param receiverName  The receiver name
 * @param receiverPhone The receiver phone
 * @param content       The address content
 * @param note         The address additional note
 * @param city          The address city, useful when showing the store location
 */
public record Address(UUID id, UUID userId, String receiverName, String receiverPhone, String content, String note, IndonesianCity city) {

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id.toString());
        jsonObject.put("user", userId.toString());
        jsonObject.put("receiver_name", receiverName);
        jsonObject.put("receiver_phone", receiverPhone);
        jsonObject.put("content", content);
        jsonObject.put("notes", note);
        jsonObject.put("city", city.getName());
        return jsonObject;
    }

}
