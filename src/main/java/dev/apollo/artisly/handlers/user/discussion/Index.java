package dev.apollo.artisly.handlers.user.discussion;

import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.DiscussionReply;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.pagination.PaginatedDiscussionReply;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.DiscussionService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.UUID;

public class Index implements APIHandler {

    @Override
    public void handle(Context context) {
        InputFilter.validateUUID("product_id", ParamField.PATH, context);
        InputFilter.validateInt("page", ParamField.QUERY, context, 1, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);
        InputFilter.validateString("sort_by", ParamField.QUERY, context, new String[]{"discussion_reply_created"});
        InputFilter.validateBoolean("ascending", ParamField.QUERY, context);


        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        try
        {
            PaginatedDiscussionReply discussions = DiscussionService.index(
                    UUID.fromString(context.pathParam("product_id")),
                    Integer.parseInt(context.queryParam("page")),
                    Integer.parseInt(context.queryParam("limit")),
                    context.queryParam("sort_by"),
                    Boolean.parseBoolean(context.queryParam("ascending"))
            );

            JSONObject response = new JSONObject();

            JSONArray discussionsArray = new JSONArray();
            for (DiscussionReply discussion : discussions.discussionReplies()) {
                JSONObject discussionObject = new JSONObject();
                JSONObject senderObject = new JSONObject();
                discussionObject.put("id", discussion.id().toString());
                discussionObject.put("product_id", discussion.productId().toString());
                discussionObject.put("content", discussion.content());
                discussionObject.put("created_at", discussion.created().toString());
                senderObject.put("id", discussion.userId().toString());
                senderObject.put("type", discussion.sender());
                try
                {
                    User user = UserService.show(discussion.userId());
                    senderObject.put("first_name", user.firstName());
                    senderObject.put("last_name", user.lastName());
                } catch (UserNotFoundException e) {
                    senderObject.put("first_name", "Anonymous");
                    senderObject.put("last_name", "User");
                }
                discussionObject.put("sender", senderObject);
                discussionsArray.add(discussionObject);
            }

            response.put("discussions", discussionsArray);
            response.put("page", discussions.page());
            response.put("limit", discussions.limit());
            response.put("total", discussions.totalDiscussionReplies());
            response.put("total_pages", discussions.totalPages());
            response.put("sort_by", discussions.column());
            response.put("ascending", discussions.asc());

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan diskusi.", "discussions", response);

        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Gagal mendapatkan diskusi. Silahkan coba lagi.");
            return;
        }



    }
}
