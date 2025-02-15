package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.StoreVet;
import dev.apollo.artisly.models.pagination.PaginatedStoreReport;
import dev.apollo.artisly.models.pagination.PaginatedStoreVet;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSStoreVet {

    public static StoreVet create(Connection connection, UUID userId, UUID storeId, String note) throws SQLException {
        String SQL = "INSERT INTO store_vets (store_vet_id, user_id, store_id, note, store_vet_created, store_vet_result, store_vet_updated) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            UUID storeVetId = UUID.randomUUID();
            LocalDate storeVetCreated = LocalDate.now();
            PreparedStatement.setString(1, storeVetId.toString());
            PreparedStatement.setString(2, userId.toString());
            PreparedStatement.setString(3, storeId.toString());
            PreparedStatement.setString(4, note);
            PreparedStatement.setDate(5, Date.valueOf(storeVetCreated));
            PreparedStatement.setBoolean(6, false);
            PreparedStatement.setDate(7, null);
            PreparedStatement.executeUpdate();
            return new StoreVet(storeVetId, storeId, userId, note, storeVetCreated, false, Optional.empty());
        }
    }

    public static Optional<StoreVet> readById(Connection connection, UUID storeVetId) throws SQLException {
        String SQL = "SELECT * FROM store_vets WHERE store_vet_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeVetId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    StoreVet storeVet = new StoreVet(
                            UUID.fromString(ResultSet.getString("store_vet_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("note"),
                            ResultSet.getDate("store_vet_created").toLocalDate(),
                            ResultSet.getBoolean("store_vet_result"),
                            ResultSet.getDate("store_vet_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_vet_updated").toLocalDate())
                    );
                    return Optional.of(storeVet);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<StoreVet> readByUserId(Connection connection, UUID userId) throws SQLException {
        String SQL = "SELECT * FROM store_vets WHERE user_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, userId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    StoreVet storeVet = new StoreVet(
                            UUID.fromString(ResultSet.getString("store_vet_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("note"),
                            ResultSet.getDate("store_vet_created").toLocalDate(),
                            ResultSet.getBoolean("store_vet_result"),
                            ResultSet.getDate("store_vet_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_vet_updated").toLocalDate())
                    );
                    return Optional.of(storeVet);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<StoreVet> readByStoreId(Connection connection, UUID storeId) throws SQLException {
        String SQL = "SELECT * FROM store_vets WHERE store_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    StoreVet storeVet = new StoreVet(
                            UUID.fromString(ResultSet.getString("store_vet_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("note"),
                            ResultSet.getDate("store_vet_created").toLocalDate(),
                            ResultSet.getBoolean("store_vet_result"),
                            ResultSet.getDate("store_vet_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_vet_updated").toLocalDate())
                    );
                    return Optional.of(storeVet);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<StoreVet> update(Connection connection, UUID storeVetId, UUID userId, UUID storeId, String note, LocalDate storeVetCreated, boolean storeVetResult, Optional<LocalDate> storeVetUpdated) throws SQLException {
        String SQL = "UPDATE store_vets SET user_id = ?, store_id = ?, note = ?, store_vet_created = ?, store_vet_result = ?, store_vet_updated = ? WHERE store_vet_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, userId.toString());
            PreparedStatement.setString(2, storeId.toString());
            PreparedStatement.setString(3, note);
            PreparedStatement.setDate(4, Date.valueOf(storeVetCreated));
            PreparedStatement.setBoolean(5, storeVetResult);
            PreparedStatement.setDate(6, storeVetUpdated.isPresent() ? Date.valueOf(storeVetUpdated.get()) : null);
            PreparedStatement.setString(7, storeVetId.toString());
            PreparedStatement.executeUpdate();
            return readById(connection, storeVetId);
        }
    }

    public static void delete(Connection connection, UUID storeVetId) throws SQLException {
        String SQL = "DELETE FROM store_vets WHERE store_vet_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeVetId.toString());
            PreparedStatement.executeUpdate();
        }
    }

    public static PaginatedStoreVet readAll(Connection connection, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int total = count(connection);
        int offset = (page - 1) * limit;
        String SQL = "SELECT * FROM store_vets ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setInt(1, limit);
            PreparedStatement.setInt(2, offset);
            try(var ResultSet = PreparedStatement.executeQuery()) {
                List<StoreVet> storeVets = new ArrayList<>();
                while(ResultSet.next()) {
                    StoreVet storeVet = new StoreVet(
                            UUID.fromString(ResultSet.getString("store_vet_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("note"),
                            ResultSet.getDate("store_vet_created").toLocalDate(),
                            ResultSet.getBoolean("store_vet_result"),
                            ResultSet.getDate("store_vet_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_vet_updated").toLocalDate())
                    );
                    storeVets.add(storeVet);
                }
                return new PaginatedStoreVet(storeVets, sort_by, ascending, page, limit, (int) Math.ceil((double) total / limit), total);
            }
        }
    }

    public static List<StoreVet> readAll(Connection connection) throws SQLException {
        String SQL = "SELECT * FROM store_vets";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            try(var ResultSet = PreparedStatement.executeQuery()) {
                List<StoreVet> storeVets = new ArrayList<>();
                while(ResultSet.next()) {
                    StoreVet storeVet = new StoreVet(
                            UUID.fromString(ResultSet.getString("store_vet_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("note"),
                            ResultSet.getDate("store_vet_created").toLocalDate(),
                            ResultSet.getBoolean("store_vet_result"),
                            ResultSet.getDate("store_vet_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_vet_updated").toLocalDate())
                    );
                    storeVets.add(storeVet);
                }
                return storeVets;
            }
        }
    }

    public static int count(Connection connection) throws SQLException
    {
        String SQL = "SELECT COUNT(*) FROM store_vets";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    return ResultSet.getInt(1);
                }
            }
        }
        return 0;
    }
}
