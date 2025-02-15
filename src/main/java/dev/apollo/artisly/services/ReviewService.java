package dev.apollo.artisly.services;


import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSImmutableProduct;
import dev.apollo.artisly.datalayer.CRUDSOrder;
import dev.apollo.artisly.datalayer.CRUDSOrderRecord;
import dev.apollo.artisly.datalayer.CRUDSReview;
import dev.apollo.artisly.exceptions.InvalidOrderException;
import dev.apollo.artisly.exceptions.ReviewExpired;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedReview;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReviewService {

    public static Review create(UUID orderId, int reviewRating, String reviewContent) throws SQLException, InvalidOrderException, ReviewExpired {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Order> optionalOrder = CRUDSOrder.readByOrderId(connection, orderId);
            if (!optionalOrder.isPresent()) {
                throw new InvalidOrderException("Order dengan id " + orderId + " tidak ditemukan");
            }
            Order order = optionalOrder.get();

            Optional<Review> optionalReview = CRUDSReview.readByOrderId(connection, orderId);
            if (optionalReview.isPresent()) {
                throw new ReviewExpired("Order dengan id " + orderId + " sudah memiliki review");
            }

            Optional<ImmutableProduct> optionalImmutableProduct = CRUDSImmutableProduct.readByProductId(connection, order.immuteableProductId());
            if (!optionalImmutableProduct.isPresent()) {
                throw new InvalidOrderException("Product tidak ditemukan");
            }

            if (order.orderStatus() != OrderStatus.COMPLETED)
            {
                throw new InvalidOrderException("Order belum selesai");
            }

            List<OrderRecord> orderRecords = CRUDSOrderRecord.readByOrderId(connection, orderId);
            LocalDate completedDate = orderRecords.get(orderRecords.size() - 1).date();

            if (LocalDate.now().isAfter(completedDate.plusDays(30))) {
                throw new ReviewExpired("Tidak dapat memberikan review setelah " + completedDate.plusDays(30));
            }

            return CRUDSReview.create(connection, orderId, reviewRating, reviewContent);

        }
    }

    public static void delete(UUID reviewId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            CRUDSReview.delete(connection, reviewId);
        }
    }

    public static double getAverageRating(UUID productId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.getAverageRating(connection, productId);
        }
    }

    public static double getAverageRatingOfStore(UUID storeId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.getAverageRatingOfStore(connection, storeId);
        }
    }

    public static int getRatingCount(UUID productId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.countByProductId(connection, productId);
        }
    }

    public static int getRatingCountOfStore(UUID storeId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.countByStoreId(connection, storeId);
        }
    }

    public static PaginatedReview index(UUID productId, int page, int size, String sortBy, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.readByProductId(connection, productId, page, size, sortBy, ascending);
        }
    }

    public static Optional<Review> getReviewByOrderId(UUID orderId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSReview.readByOrderId(connection, orderId);
        }
    }

}
