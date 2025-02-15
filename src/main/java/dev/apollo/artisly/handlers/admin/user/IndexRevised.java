package dev.apollo.artisly.handlers.admin.user;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.models.pagination.PaginatedUser;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;

public class IndexRevised implements APIHandler {
    @Override
    public void handle(Context context) {

        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        if (context.queryParam("search") != null) {
            InputFilter.validateString("search", ParamField.QUERY, context, 255);
        }

        InputFilter.validateInt("page", ParamField.QUERY, context, 1, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);
        InputFilter.validateString("sort_by", ParamField.QUERY, context, new String[]{"first_name", "last_name", "email", "created"});
        InputFilter.validateBoolean("ascending", ParamField.QUERY, context);

        if (context.attribute("hasErrors") != null) {
            return;
        }

        int page = Integer.parseInt(context.queryParam("page"));
        int limit = Integer.parseInt(context.queryParam("limit"));
        String sortBy = context.queryParam("sort_by");
        boolean ascending = Boolean.parseBoolean(context.queryParam("ascending"));

        try
        {
            PaginatedUser users;
            if (context.queryParam("search") != null)
            {
                users = UserService.search(context.queryParam("search"), page, limit, sortBy, ascending);
            }
            else
            {
                users = UserService.index(page, limit, sortBy, ascending);
            }
            JSONObject response = new JSONObject();
            JSONArray usersArray = new JSONArray();
            for (User user : users.users())
            {
                JSONObject userObject = new JSONObject();
                userObject.put("id", user.id().toString());
                userObject.put("first_name", user.firstName());
                userObject.put("last_name", user.lastName());
                userObject.put("email", user.email());
                userObject.put("phone_number", user.phoneNumber());
                userObject.put("is_email_verified", user.emailVerified().isPresent());
                userObject.put("is_ktp_verified", user.nomorKTP().isPresent());
                userObject.put("created", user.created().toString());
                usersArray.add(userObject);
            }

            response.put("users", usersArray);
            response.put("total_users", users.totalUsers());
            response.put("page", users.page());
            response.put("limit", users.limit());
            response.put("total_pages", users.totalPages());
            response.put("sort_by", users.column());
            response.put("ascending", users.asc());

            StandarizedResponses.success(context,
                    "USERS_INDEX_SUCCESS",
                    "Successfully retrieved users",
                    "response",
                    response);


        } catch (SQLException e) {
            StandarizedResponses.internalError(context, e.getMessage());
        }
    }
}
