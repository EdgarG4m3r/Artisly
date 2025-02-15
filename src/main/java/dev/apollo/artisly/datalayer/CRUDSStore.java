package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.pagination.PaginatedStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSStore {

    public static Store create(Connection connection, String storeName, String storeNote, UUID userId) throws SQLException {
        String query = "INSERT INTO stores (store_id, store_name, store_note, user_id) VALUES (?, ?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            UUID storeId = UUID.randomUUID();
            LocalDate storeCreated = LocalDate.now();
            ps.setString(1, storeId.toString());
            ps.setString(2, storeName);
            ps.setString(3, storeNote);
            ps.setString(4, userId.toString());
            ps.executeUpdate();
            return new Store(storeId, storeName, storeNote, storeCreated, userId, Optional.empty());
        }
    }

    public static Optional<Store> readById(Connection connection, UUID storeId) throws SQLException {
        String query = "SELECT * FROM stores WHERE store_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, storeId.toString());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    Store store = new Store(
                            UUID.fromString(rs.getString("store_id")),
                            rs.getString("store_name"),
                            rs.getString("store_note"),
                            rs.getDate("store_created").toLocalDate(),
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("store_vet_id") == null ? Optional.empty() : Optional.of(UUID.fromString(rs.getString("store_vet_id")))
                    );
                    return Optional.of(store);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Store> readByUserId(Connection connection, UUID userId) throws SQLException {
        String query = "SELECT * FROM stores WHERE user_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userId.toString());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    Store store = new Store(
                            UUID.fromString(rs.getString("store_id")),
                            rs.getString("store_name"),
                            rs.getString("store_note"),
                            rs.getDate("store_created").toLocalDate(),
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("store_vet_id") == null ? Optional.empty() : Optional.of(UUID.fromString(rs.getString("store_vet_id")))
                    );
                    return Optional.of(store);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean update(Connection connection, UUID storeId, String storeName, String storeNote, Optional<UUID> storeVetId) throws SQLException {
        String query = "UPDATE stores SET store_name = ?, store_note = ?, store_vet_id = ? WHERE store_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, storeName);
            ps.setString(2, storeNote);
            ps.setString(3, storeVetId.isEmpty() ? null : storeVetId.get().toString());
            ps.setString(4, storeId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public static PaginatedStore search(Connection connection, String query, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int total = count(connection, query, sort_by, ascending);
        int totalPages = (int) Math.ceil((double) total / (double) limit);
        String SQL = "SELECT * FROM stores WHERE store_name LIKE ? OR store_note LIKE ? SORT BY ? " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, sort_by);
            ps.setInt(4, limit);
            ps.setInt(5, (page - 1) * limit);
            try(ResultSet rs = ps.executeQuery()) {
                List<Store> stores = new ArrayList<>();
                while (rs.next()) {
                    Store store = new Store(
                            UUID.fromString(rs.getString("store_id")),
                            rs.getString("store_name"),
                            rs.getString("store_note"),
                            rs.getDate("store_created").toLocalDate(),
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("store_vet_id") == null ? Optional.empty() : Optional.of(UUID.fromString(rs.getString("store_vet_id")))
                    );
                    stores.add(store);
                }
                return new PaginatedStore(stores, sort_by, ascending, page, limit, totalPages, total);
            }
        }

    }

    public static PaginatedStore getAll(Connection connection, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int total = count(connection);
        int totalPages = (int) Math.ceil((double) total / (double) limit);
        String SQL = "SELECT * FROM stores SORT BY ? " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, sort_by);
            ps.setInt(2, limit);
            ps.setInt(3, (page - 1) * limit);
            try(ResultSet rs = ps.executeQuery()) {
                List<Store> stores = new ArrayList<>();
                while (rs.next()) {
                    Store store = new Store(
                            UUID.fromString(rs.getString("store_id")),
                            rs.getString("store_name"),
                            rs.getString("store_note"),
                            rs.getDate("store_created").toLocalDate(),
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("store_vet_id") == null ? Optional.empty() : Optional.of(UUID.fromString(rs.getString("store_vet_id")))
                    );
                    stores.add(store);
                }
                return new PaginatedStore(stores, sort_by, ascending, page, limit, totalPages, total);
            }
        }

    }

    public static int count(Connection connection, String query, String sort_by, boolean ascending) throws SQLException {
        String SQL = "SELECT COUNT(*) FROM stores WHERE store_name LIKE ? OR store_note LIKE ? SORT BY ? " + (ascending ? "ASC" : "DESC");
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, sort_by);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int count(Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) FROM stores";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

}
