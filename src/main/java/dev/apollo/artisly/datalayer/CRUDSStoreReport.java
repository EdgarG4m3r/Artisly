package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.StoreReport;
import dev.apollo.artisly.models.pagination.PaginatedStoreReport;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSStoreReport {

    public static StoreReport create(Connection connection, UUID storeId, UUID userId, Optional<UUID> immutableProductId, String storeReportReason) throws SQLException {
        String SQL = "INSERT INTO store_reports (store_report_id, store_id, user_id, copy_of_product_id, store_report_reason, store_report_resolved, store_report_created, store_report_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            UUID storeReportId = UUID.randomUUID();
            LocalDate storeReportCreated = LocalDate.now();
            PreparedStatement.setString(1, storeReportId.toString());
            PreparedStatement.setString(2, storeId.toString());
            PreparedStatement.setString(3, userId.toString());
            PreparedStatement.setString(4, immutableProductId.isPresent() ? immutableProductId.get().toString() : null);
            PreparedStatement.setString(5, storeReportReason);
            PreparedStatement.setBoolean(6, false);
            PreparedStatement.setDate(7, Date.valueOf(storeReportCreated));
            PreparedStatement.setDate(8, null);
            PreparedStatement.executeUpdate();
            return new StoreReport(storeReportId, storeId, userId, immutableProductId, storeReportReason, false, storeReportCreated, Optional.empty());
        }
    }

    public static Optional<StoreReport> readById(Connection connection, UUID storeReportId) throws SQLException {
        String SQL = "SELECT * FROM store_reports WHERE store_report_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeReportId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    StoreReport storeReport = new StoreReport(
                            UUID.fromString(ResultSet.getString("store_report_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("copy_of_product_id") == null ? Optional.empty() : Optional.of(UUID.fromString(ResultSet.getString("copy_of_product_id"))),
                            ResultSet.getString("store_report_reason"),
                            ResultSet.getBoolean("store_report_resolved"),
                            ResultSet.getDate("store_report_created").toLocalDate(),
                            ResultSet.getDate("store_report_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_report_updated").toLocalDate())
                    );
                    return Optional.of(storeReport);
                }
            }
        }
        return Optional.empty();
    }

    public static PaginatedStoreReport readByUserId(Connection connection, UUID userId, int page, int pageSize, String sort_by, boolean ascending) throws SQLException
    {
        int total = countByUserId(connection, userId);
        int offset = (page - 1) * pageSize;
        String SQL = "SELECT * FROM store_reports WHERE user_id = ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, userId.toString());
            PreparedStatement.setInt(2, pageSize);
            PreparedStatement.setInt(3, offset);
            try(var ResultSet = PreparedStatement.executeQuery()) {
                List<StoreReport> storeReports = new ArrayList<>();
                while(ResultSet.next()) {
                    StoreReport storeReport = new StoreReport(
                            UUID.fromString(ResultSet.getString("store_report_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("copy_of_product_id") == null ? Optional.empty() : Optional.of(UUID.fromString(ResultSet.getString("copy_of_product_id"))),
                            ResultSet.getString("store_report_reason"),
                            ResultSet.getBoolean("store_report_resolved"),
                            ResultSet.getDate("store_report_created").toLocalDate(),
                            ResultSet.getDate("store_report_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_report_updated").toLocalDate())
                    );
                    storeReports.add(storeReport);
                }
                return new PaginatedStoreReport(storeReports, sort_by, ascending, page, pageSize, (int) Math.ceil((double) total / pageSize), total);
            }
        }
    }

    public static PaginatedStoreReport readByStoreId(Connection connection, UUID storeId, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int total = countByStoreId(connection, storeId);
        int offset = (page - 1) * limit;
        String SQL = "SELECT * FROM store_reports WHERE store_id = ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeId.toString());
            PreparedStatement.setInt(2, limit);
            PreparedStatement.setInt(3, offset);
            try(var ResultSet = PreparedStatement.executeQuery()) {
                List<StoreReport> storeReports = new ArrayList<>();
                while(ResultSet.next()) {
                    StoreReport storeReport = new StoreReport(
                            UUID.fromString(ResultSet.getString("store_report_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("copy_of_product_id") == null ? Optional.empty() : Optional.of(UUID.fromString(ResultSet.getString("copy_of_product_id"))),
                            ResultSet.getString("store_report_reason"),
                            ResultSet.getBoolean("store_report_resolved"),
                            ResultSet.getDate("store_report_created").toLocalDate(),
                            ResultSet.getDate("store_report_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_report_updated").toLocalDate())
                    );
                    storeReports.add(storeReport);
                }
                return new PaginatedStoreReport(storeReports, sort_by, ascending, page, limit, (int) Math.ceil((double) total / limit), total);
            }
        }
    }

    public static PaginatedStoreReport readAll(Connection connection, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int total = count(connection);
        int offset = (page - 1) * limit;
        String SQL = "SELECT * FROM store_reports ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setInt(1, limit);
            PreparedStatement.setInt(2, offset);
            try(var ResultSet = PreparedStatement.executeQuery()) {
                List<StoreReport> storeReports = new ArrayList<>();
                while(ResultSet.next()) {
                    StoreReport storeReport = new StoreReport(
                            UUID.fromString(ResultSet.getString("store_report_id")),
                            UUID.fromString(ResultSet.getString("store_id")),
                            UUID.fromString(ResultSet.getString("user_id")),
                            ResultSet.getString("copy_of_product_id") == null ? Optional.empty() : Optional.of(UUID.fromString(ResultSet.getString("copy_of_product_id"))),
                            ResultSet.getString("store_report_reason"),
                            ResultSet.getBoolean("store_report_resolved"),
                            ResultSet.getDate("store_report_created").toLocalDate(),
                            ResultSet.getDate("store_report_updated") == null ? Optional.empty() : Optional.of(ResultSet.getDate("store_report_updated").toLocalDate())
                    );
                    storeReports.add(storeReport);
                }
                return new PaginatedStoreReport(storeReports, sort_by, ascending, page, limit, (int) Math.ceil((double) total / limit), total);
            }
        }
    }

    public static List<StoreReport> readAll(Connection connection) throws SQLException
    {
        List<StoreReport> storeReports = new ArrayList<>();
        String SQL = "SELECT * FROM store_reports ORDER BY store_report_created ASC";
        try (PreparedStatement ps = connection.prepareStatement(SQL))
        {
            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    StoreReport storeReport = new StoreReport(
                            UUID.fromString(rs.getString("store_report_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("copy_of_product_id") == null ? Optional.empty() : Optional.of(UUID.fromString(rs.getString("copy_of_product_id"))),
                            rs.getString("store_report_reason"),
                            rs.getBoolean("store_report_resolved"),
                            rs.getDate("store_report_created").toLocalDate(),
                            rs.getDate("store_report_updated") == null ? Optional.empty() : Optional.of(rs.getDate("store_report_updated").toLocalDate())
                    );
                    storeReports.add(storeReport);
                }
            }
        }
        return storeReports;
    }

    public static void update(Connection connection, UUID storeReportId, boolean storeReportResolved) throws SQLException {
        String SQL = "UPDATE store_reports SET store_report_resolved = ?, store_report_updated = ? WHERE store_report_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setBoolean(1, storeReportResolved);
            PreparedStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            PreparedStatement.setString(3, storeReportId.toString());
            PreparedStatement.executeUpdate();
        }
    }

    public static void delete(Connection connection, UUID storeReportId) throws SQLException {
        String SQL = "DELETE FROM store_reports WHERE store_report_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeReportId.toString());
            PreparedStatement.executeUpdate();
        }
    }

    public static int count(Connection connection) throws SQLException {
        String SQL = "SELECT COUNT(*) FROM store_reports";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    return ResultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int countByUserId(Connection connection, UUID userId) throws SQLException {
        String SQL = "SELECT COUNT(*) FROM store_reports WHERE user_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, userId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    return ResultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int countByStoreId(Connection connection, UUID storeId) throws SQLException {
        String SQL = "SELECT COUNT(*) FROM store_reports WHERE store_id = ?";
        try(var PreparedStatement = connection.prepareStatement(SQL)) {
            PreparedStatement.setString(1, storeId.toString());
            try(var ResultSet = PreparedStatement.executeQuery()) {
                if(ResultSet.next()) {
                    return ResultSet.getInt(1);
                }
            }
        }
        return 0;
    }

}
