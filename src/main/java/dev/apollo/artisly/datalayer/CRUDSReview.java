package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.Review;
import dev.apollo.artisly.models.pagination.PaginatedReview;
import dev.apollo.artisly.services.AlgoliaService;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSReview {


    public static Review create(Connection connection, UUID order_id, int reviewRating, String reviewContent) throws SQLException
    {
        String sql = "INSERT INTO reviews (review_id, order_id, review_rating, review_date, review_content) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            UUID reviewId = UUID.randomUUID();
            LocalDate reviewDate = LocalDate.now();
            ps.setString(1, reviewId.toString());
            ps.setString(2, order_id.toString());
            ps.setInt(3, reviewRating);
            ps.setDate(4, Date.valueOf(reviewDate));
            ps.setString(5, reviewContent);
            ps.executeUpdate();
            return new Review(reviewId, order_id, reviewRating, reviewDate, reviewContent);
        }
    }

    public static Review readById(Connection connection, UUID reviewId) throws SQLException
    {
        String sql = "SELECT * FROM reviews WHERE review_id = ? AND review_rating != -1";
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, reviewId.toString());
            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    //UUID id, UUID userId, UUID orderId, int rating, LocalDate date, String content
                    Review review = new Review(
                            UUID.fromString(rs.getString("review_id")),
                            UUID.fromString(rs.getString("order_id")),
                            rs.getInt("review_rating"),
                            rs.getDate("review_date").toLocalDate(),
                            rs.getString("review_content")
                    );
                    return review;
                }
                return null;
            }
        }
    }

    public static double getAverageRating(Connection connection, UUID productId) throws SQLException {
        String sql = "SELECT AVG(review_rating) FROM reviews " +
                "JOIN orders ON reviews.order_id = orders.order_id " +
                "JOIN immutable_products ON orders.copy_of_product_id = immutable_products.immutable_product_id " +
                "WHERE immutable_products.product_id = ? AND review_rating != -1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (int) Math.round(rs.getDouble(1));
                }
            }
        }
        return 0;
    }


    public static double getAverageRatingOfStore(Connection connection, UUID storeId) throws SQLException
    {
        //Join with Order table to get the store id, in review table, we reference order_id as a foreign key, and order table has a store_id
        String sql = "SELECT AVG(review_rating) FROM reviews JOIN orders ON reviews.order_id = orders.order_id WHERE orders.store_id = ? AND review_rating != -1";
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, storeId.toString());
            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return (int) Math.round(rs.getDouble(1));
                }
            }
        }
        return 0;
    }

    //countByStoreId(connection, storeId);
    public static int countByStoreId(Connection connection, UUID storeId) throws SQLException
    {
        String sql = "SELECT COUNT(*) FROM reviews JOIN orders ON reviews.order_id = orders.order_id WHERE orders.store_id = ? AND review_rating != -1";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            PreparedStatement.setString(1, storeId.toString());
            try(var ResultSet = PreparedStatement.executeQuery())
            {
                if(ResultSet.next())
                {
                    return ResultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static Optional<Review> readByOrderId(Connection connection, UUID orderId) throws SQLException
    {
        String sql = "SELECT * FROM reviews WHERE order_id = ? AND review_rating != -1";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            PreparedStatement.setString(1, orderId.toString());
            try(ResultSet rs = PreparedStatement.executeQuery())
            {
                if(rs.next())
                {
                    Review review = new Review(
                            UUID.fromString(rs.getString("review_id")),
                            UUID.fromString(rs.getString("order_id")),
                            rs.getInt("review_rating"),
                            rs.getDate("review_date").toLocalDate(),
                            rs.getString("review_content")
                    );
                    return Optional.of(review);
                }
            }
        }
        return Optional.empty();
    }

    public static PaginatedReview readByProductId(Connection connection, UUID productId, int page, int pageSize, String sort_by, boolean ascending) throws SQLException
    {
        int reviewCount = countByProductId(connection, productId);
        int pageCount = (int) Math.ceil((double) reviewCount / pageSize);
        if(page > pageCount)
        {
            page = pageCount;
        }
        if(page < 1)
        {
            page = 1;
        }

        //String sql = "SELECT * FROM reviews WHERE product_id = ? AND review_rating != -1 ORDER BY " + sort_by + (ascending ? " ASC" : " DESC") + " LIMIT ? OFFSET ?";
        String SQL = "SELECT * FROM reviews JOIN orders ON reviews.order_id = orders.order_id JOIN immutable_products ON orders.copy_of_product_id = immutable_products.immutable_product_id WHERE immutable_products.product_id = ? AND review_rating != -1 ORDER BY " + sort_by + (ascending ? " ASC" : " DESC") + " LIMIT ? OFFSET ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL))
        {
            ps.setString(1, productId.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            try(ResultSet rs = ps.executeQuery())
            {
                List<Review> reviews = new LinkedList<>();
                while(rs.next())
                {
                    Review review = new Review(
                            UUID.fromString(rs.getString("review_id")),
                            UUID.fromString(rs.getString("order_id")),
                            rs.getInt("review_rating"),
                            rs.getDate("review_date").toLocalDate(),
                            rs.getString("review_content")
                    );
                    reviews.add(review);
                }
                return new PaginatedReview(reviews, sort_by, ascending, page, pageSize, pageCount, reviewCount);
            }
        }
    }

    public static PaginatedReview readByProductId(Connection connection, UUID productId, int page, int pageSize) throws SQLException
    {
        return readByProductId(connection, productId, page, pageSize, "review_date", false);
    }

    public static boolean update(Connection connection, UUID reviewId, int reviewRating, String reviewContent) throws SQLException
    {
        String sql = "UPDATE reviews SET review_rating = ?, review_content = ? WHERE review_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, reviewRating);
            ps.setString(2, reviewContent);
            ps.setString(3, reviewId.toString());
            int result = ps.executeUpdate();
            if (result > 0)
            {
                return true;
            }
            else
            {
                return false;
            }

        }
    }

    //instead of deleting, we just set the review content to "deleted" and set the rating to -1
    public static boolean delete(Connection connection, UUID reviewId) throws SQLException
    {
        String sql = "UPDATE reviews SET review_rating = ?, review_content = ? WHERE review_id = ?";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            PreparedStatement.setInt(1, -1);
            PreparedStatement.setString(2, "deleted");
            PreparedStatement.setString(3, reviewId.toString());

            int result = PreparedStatement.executeUpdate();
            if (result > 0)
            {
                Review review = readById(connection, reviewId);
                return true;
            }
            else
            {
                return false;
            }

        }
    }
    public static int countByProductId(Connection connection, UUID productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews " +
                "JOIN orders ON reviews.order_id = orders.order_id " +
                "JOIN immutable_products ON orders.copy_of_product_id = immutable_products.immutable_product_id " +
                "WHERE immutable_products.product_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productId.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }


}
