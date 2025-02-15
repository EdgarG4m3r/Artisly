package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSCategory {

    public static Category create(Connection connection, String categoryName, String categoryDescription) throws SQLException {
        String SQL = "INSERT INTO categories (category_id, category_name, category_description) VALUES (?, ?, ?)";
        try(var ps = connection.prepareStatement(SQL)) {
            UUID categoryId = UUID.randomUUID();
            ps.setString(1, categoryId.toString());
            ps.setString(2, categoryName);
            ps.setString(3, categoryDescription);
            ps.executeUpdate();
            return new Category(categoryId, categoryName, categoryDescription);
        }
    }

    public static Optional<Category> readById(Connection connection, UUID categoryId) throws SQLException {
        String SQL = "SELECT * FROM categories WHERE category_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, categoryId.toString());
            try(var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category(
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("category_name"),
                            rs.getString("category_description")
                    );
                    return Optional.of(category);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Category> readByName(Connection connection, String categoryName) throws SQLException {
        String SQL = "SELECT * FROM categories WHERE category_name = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, categoryName);
            try(var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category(
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("category_name"),
                            rs.getString("category_description")
                    );
                    return Optional.of(category);
                }
            }
        }
        return Optional.empty();
    }

    public static List<Category> readAll(Connection connection) throws SQLException {
        String SQL = "SELECT * FROM categories";
        try(var ps = connection.prepareStatement(SQL)) {
            try(var rs = ps.executeQuery()) {
                List<Category> categories = new ArrayList<>();
                while (rs.next()) {
                    Category category = new Category(
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("category_name"),
                            rs.getString("category_description")
                    );
                    categories.add(category);
                }
                return categories;
            }
        }
    }

    public static boolean update(Connection connection, UUID categoryId, String categoryName, String categoryDescription) throws SQLException {
        String SQL = "UPDATE categories SET category_name = ?, category_description = ? WHERE category_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, categoryName);
            ps.setString(2, categoryDescription);
            ps.setString(3, categoryId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection connection, UUID categoryId) throws SQLException {
        String SQL = "DELETE FROM categories WHERE category_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, categoryId.toString());
            return ps.executeUpdate() > 0;
        }
    }

}
