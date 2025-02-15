package dev.apollo.artisly.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * @param id          The ID of the category
 * @param name        The name of the category
 * @param description The description of the category
 */

public record Category(UUID id, String name, String description) {

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id.toString());
        jsonObject.put("name", name);
        jsonObject.put("description", description);
        return jsonObject;
    }

}
