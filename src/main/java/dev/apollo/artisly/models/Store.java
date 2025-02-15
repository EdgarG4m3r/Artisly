package dev.apollo.artisly.models;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.services.MediaService;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @param id           The ID of the store
 * @param name         The name of the store
 * @param note         The note of the store
 * @param created      The date the store was created
 * @param userId            The ID of the user who created the store
 * @param storeVetId        The ID of the vet who verified the store, null if not verified yet
 */
public record Store(UUID id, String name, String note, LocalDate created, UUID userId, Optional<UUID> storeVetId) {

    public JSONObject toJSON()
    {
        JSONObject storeJson = new JSONObject();
        storeJson.put("id", id);
        storeJson.put("name", name);
        storeJson.put("note", note);
        storeJson.put("created", created.toString());
        storeJson.put("user_id", userId);
        storeJson.put("store_vet_id", storeVetId.isPresent() ? storeVetId.get() : null);
        storeJson.put("is_verified", storeVetId.isPresent());
        storeJson.put("pictures", getStorePictures());
        return storeJson;
    }

    public JSONObject getStorePictures()
    {
        JSONObject storePicturesJson = new JSONObject();
        Artisly.instance.getMediaService().getStoreBannerUrl(id);
        Artisly.instance.getMediaService().getStoreLogoUrl(id);

        storePicturesJson.put("banner", Artisly.instance.getMediaService().getStoreBannerUrl(id));
        storePicturesJson.put("logo", Artisly.instance.getMediaService().getStoreLogoUrl(id));

        return storePicturesJson;
    }

}
