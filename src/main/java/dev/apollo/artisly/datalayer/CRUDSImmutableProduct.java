package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.ImmutableProduct;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.pagination.PaginatedProduct;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSImmutableProduct {

    public static ImmutableProduct create(Connection connection, Product product) throws SQLException {
        String sql = "INSERT INTO immutable_products (immutable_product_id, product_id, store_id, category_id, product_name, product_description, product_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            UUID immutableProductId = UUID.randomUUID();
            ps.setString(1, immutableProductId.toString());
            ps.setString(2, product.id().toString());
            ps.setString(3, product.storeId().toString());
            ps.setString(4, product.categoryId().toString());
            ps.setString(5, product.name());
            ps.setString(6, product.description());
            ps.setDouble(7, product.price());
            ps.executeUpdate();
            return new ImmutableProduct(immutableProductId, product.id(), product.storeId(), product.categoryId(), product.name(), product.description(), product.price(), product.created(), product.updated());
        }
    }

    public static Optional<ImmutableProduct> readByProductId(Connection connection, UUID immutableProduct) throws SQLException {
        String sql = "SELECT * FROM immutable_products WHERE immutable_product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, immutableProduct.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    ImmutableProduct product = new ImmutableProduct(
                            UUID.fromString(resultSet.getString("immutable_product_id")),
                            UUID.fromString(resultSet.getString("product_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("category_id")),
                            resultSet.getString("product_name"),
                            resultSet.getString("product_description"),
                            resultSet.getDouble("product_price"),
                            resultSet.getDate("product_created").toLocalDate(),
                            resultSet.getDate("product_updated").toLocalDate()
                    );
                    return Optional.of(product);
                }
            }
        }
        return Optional.empty();
    }
}
