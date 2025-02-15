package dev.apollo.artisly.handlers.user.review;

import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.Order;
import dev.apollo.artisly.models.Review;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.pagination.PaginatedReview;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.OrderService;
import dev.apollo.artisly.services.ReviewService;
import dev.apollo.artisly.services.UserService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Index implements APIHandler {
    @Override
    public void handle(Context context) {
        InputFilter.validateUUID("product_id", ParamField.PATH, context);
        InputFilter.validateInt("page", ParamField.QUERY, context, 1, Integer.MAX_VALUE);
        InputFilter.validateInt("limit", ParamField.QUERY, context, 1, 50);
        InputFilter.validateString("sort_by", ParamField.QUERY, context, new String[]{"review_date", "review_rating"});
        InputFilter.validateBoolean("ascending", ParamField.QUERY, context);

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        try
        {
            UUID productID = UUID.fromString(context.pathParam("product_id"));
            int page = Integer.parseInt(context.queryParam("page"));
            int limit = Integer.parseInt(context.queryParam("limit"));
            String sortBy = context.queryParam("sort_by");
            boolean ascending = Boolean.parseBoolean(context.queryParam("ascending"));
            PaginatedReview reviews = ReviewService.index(productID, page, limit, sortBy, ascending);
            int totalReviews = ReviewService.getRatingCount(productID);
            double averageRating = ReviewService.getAverageRating(productID);
            averageRating = Math.round(averageRating * 10.0) / 10.0;

            JSONArray reviewsArray = new JSONArray();
            for (Review review : reviews.reviews()) {
                JSONObject userObject = new JSONObject();
                try
                {
                    Order order = OrderService.getOrder(review.orderId()).get();
                    User user = UserService.show(order.userId());
                    userObject.put("id", user.id().toString());
                    userObject.put("first_name", user.firstName());
                    userObject.put("last_name", user.lastName());
                } catch (Exception e) {
                    userObject.put("id", UUID.randomUUID().toString());
                    userObject.put("first_name", "Pengguna");
                    userObject.put("last_name", "Anonim");
                }


                JSONObject reviewObject = new JSONObject();
                reviewObject.put("id", review.id().toString());
                reviewObject.put("rating", review.rating());
                reviewObject.put("review", review.content());
                reviewObject.put("created_at", review.date().toString());
                reviewObject.put("user", userObject);

                reviewsArray.add(reviewObject);
            }

            JSONObject response = new JSONObject();
            response.put("reviews", reviewsArray);
            response.put("total_reviews", totalReviews);
            response.put("average_rating", averageRating);
            response.put("page", reviews.page());
            response.put("limit", reviews.limit());
            response.put("total_pages", reviews.totalPages());
            response.put("sort_by", reviews.column());
            response.put("ascending", reviews.asc());


            StandarizedResponses.success(context, "SUCCESS", "Berhasil mendapatkan review produk.", "reviews", response);
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Terjadi kesalahan saat mengambil data review produk. Silahkan coba lagi nanti.");
        }
    }
}
