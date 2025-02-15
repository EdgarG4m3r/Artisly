package dev.apollo.artisly.models;

import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * @param id              The ID of the store vet
 * @param storeId         The ID of the store that was verified
 * @param userId The date the store vet was created
 * @param note  The result of the verification, true if the store is verified
 * @param created  The date the store vet was last updated
 * @param result  The result of the verification, true if the store is verified
 * @param updated  The date the store vet was last updated
 */

public record StoreVet(UUID id, UUID storeId, UUID userId, String note, LocalDate created, boolean result, Optional<LocalDate> updated) {

    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id.toString());
        jsonObject.put("store_id", storeId.toString());
        jsonObject.put("user_id", userId.toString());
        jsonObject.put("note", note);
        jsonObject.put("created", created.toString());
        jsonObject.put("result", result);
        jsonObject.put("updated", updated.isPresent() ? updated.get().toString() : null);
        return jsonObject;
    }
}
