package dev.apollo.artisly.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * @param id       The ID of the store report
 * @param storeId             The ID of the store that was reported
 * @param userId              The ID of the user who reported the store
 * @param immuteableProductId     The ID of the product that was reported. Null if no product was reported
 * @param reason   The reason why the store was reported
 * @param resolved Whether the store report has been resolved
 * @param created  The date the store report was created
 * @param updated  The date the store report was last updated
 */

public record StoreReport(UUID id, UUID storeId, UUID userId, Optional<UUID> immuteableProductId, String reason, boolean resolved, LocalDate created, Optional<LocalDate> updated) {

    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id.toString());
        jsonObject.put("store_id", storeId.toString());
        jsonObject.put("user_id", userId.toString());
        jsonObject.put("immuteable_product_id", immuteableProductId.isPresent() ? immuteableProductId.get().toString() : null);
        jsonObject.put("reason", reason);
        jsonObject.put("resolved", resolved);
        jsonObject.put("created", created.toString());
        jsonObject.put("updated", updated.isPresent() ? updated.get().toString() : null);
        return jsonObject;
    }

}
