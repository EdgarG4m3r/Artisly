package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSCategory;
import dev.apollo.artisly.exceptions.CategoryNotExist;
import dev.apollo.artisly.models.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryService {

    public static List<Category> getCategories() throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSCategory.readAll(connection);
        }
    }

    public static Optional<Category> getCategory(UUID id) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSCategory.readById(connection, id);
        }
    }

    public static Category createCategory(String name, String description) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSCategory.create(connection, name, description);
        }
    }

    public static boolean updateCategory(UUID id, String name, String description) throws SQLException, CategoryNotExist {
        if (getCategory(id).isEmpty())
        {
            throw new CategoryNotExist("Kategori dengan ID " + id + " tidak ditemukan");
        }
        try (Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSCategory.update(connection, id, name, description) ? true : false;
        }
    }

    public static boolean deleteCategory(UUID id) throws SQLException, CategoryNotExist {
        if (getCategory(id).isEmpty())
        {
            throw new CategoryNotExist("Kategori dengan ID " + id + " tidak ditemukan");
        }
        try (Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSCategory.delete(connection, id) ? true : false;
        }
    }

}
