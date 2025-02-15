package dev.apollo.artisly.models;

import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * @param id      The ID of the discussion reply
 * @param userId                 The ID of the user who created the discussion reply
 * @param sender  If seller or buyer who created the discussion reply, it is either "SELLER" or "BUYER"
 * @param content The content of the discussion reply
 * @param created The date the discussion reply was created
 */

public record DiscussionReply(UUID id, UUID productId, UUID userId, String sender, String content, LocalDate created) {

}
