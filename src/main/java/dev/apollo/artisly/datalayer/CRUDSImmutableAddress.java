package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.ImmutableAddress;
import dev.apollo.artisly.models.IndonesianCity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSImmutableAddress {

    public static ImmutableAddress create(Connection connection, UUID userId, UUID addressId, String receiverName, String receiverPhone, String content, String notes, IndonesianCity city) throws SQLException {

        String SQL = "INSERT INTO immutable_addresses (immutable_address_id, address_id, user_id, address_receiver_name, address_receiver_phone, address_content, address_notes, address_city) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            UUID immutableAddressId = UUID.randomUUID();
            ps.setString(1, immutableAddressId.toString());
            ps.setString(2, addressId.toString());
            ps.setString(3, userId.toString());
            ps.setString(4, receiverName);
            ps.setString(5, receiverPhone);
            ps.setString(6, content);
            ps.setString(7, notes);
            ps.setString(8, city.getName());
            ps.executeUpdate();
            return new ImmutableAddress(immutableAddressId, userId, addressId, receiverName, receiverPhone, content, notes, city);
        }

    }

    public static Optional<ImmutableAddress> readByImmutableAddressId(Connection connection, UUID immutableAddressId) throws SQLException {
        String SQL = "SELECT * FROM immutable_addresses WHERE immutable_address_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, immutableAddressId.toString());
            try (var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city").toUpperCase().replaceAll(" ", "_"));
                    } catch (Exception e) {
                        city = IndonesianCity.JAKARTA;
                    }
                    ImmutableAddress address = new ImmutableAddress(
                            UUID.fromString(resultSet.getString("immutable_address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("address_id")),
                            resultSet.getString("address_receiver_name"),
                            resultSet.getString("address_receiver_phone"),
                            resultSet.getString("address_content"),
                            resultSet.getString("address_notes"),
                            city
                    );
                    return Optional.of(address);
                }
            }
        }
        return Optional.empty();
    }

    public static List<ImmutableAddress> readByUserId(Connection connection, UUID userId) throws SQLException {
        String SQL = "SELECT * FROM immutable_addresses WHERE user_id = ?";
        List<ImmutableAddress> addresses = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, userId.toString());
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city").toUpperCase().replaceAll(" ", "_"));
                    } catch (Exception e) {
                        city = IndonesianCity.JAKARTA;
                    }
                    ImmutableAddress address = new ImmutableAddress(
                            UUID.fromString(resultSet.getString("immutable_address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("address_id")),
                            resultSet.getString("address_receiver_name"),
                            resultSet.getString("address_receiver_phone"),
                            resultSet.getString("address_content"),
                            resultSet.getString("address_notes"),
                            city
                    );
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }

    public static List<ImmutableAddress> search(Connection connection, String query, int page, int limit, String sort_by, boolean ascending) throws SQLException {
        int offset = (page - 1) * limit;
        String SQL = "SELECT * FROM immutable_addresses WHERE address_receiver_name LIKE ? OR address_receiver_phone LIKE ? OR address_content LIKE ? OR address_notes LIKE ? OR address_city LIKE ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        List<ImmutableAddress> addresses = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, "%" + query + "%");
            ps.setString(5, "%" + query + "%");
            ps.setInt(6, limit);
            ps.setInt(7, offset);
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city"));
                    } catch (Exception e) {
                        city = IndonesianCity.JAKARTA;
                    }
                    ImmutableAddress address = new ImmutableAddress(
                            UUID.fromString(resultSet.getString("immutable_address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("address_id")),
                            resultSet.getString("address_receiver_name"),
                            resultSet.getString("address_receiver_phone"),
                            resultSet.getString("address_content"),
                            resultSet.getString("address_notes"),
                            city
                    );
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }
}

