package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.Address;
import dev.apollo.artisly.models.IndonesianCity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSAddress {

    public static Address create(Connection connection, UUID userId, String receiverName, String receiverPhone, String content, String notes, IndonesianCity city) throws SQLException {

        String SQL = "INSERT INTO addresses (address_id, user_id, address_receiver_name, address_receiver_phone, address_content, address_notes, address_city) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            UUID addressId = UUID.randomUUID();
            ps.setString(1, addressId.toString());
            ps.setString(2, userId.toString());
            ps.setString(3, receiverName);
            ps.setString(4, receiverPhone);
            ps.setString(5, content);
            ps.setString(6, notes);
            ps.setString(7, city.getName());
            ps.executeUpdate();
            return new Address(addressId, userId, receiverName, receiverPhone, content, notes, city);
        }

    }

    public static Optional<Address> readByAddressId(Connection connection, UUID addressId) throws SQLException {
        String SQL = "SELECT * FROM addresses WHERE address_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, addressId.toString());
            try(var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city").toUpperCase().replaceAll(" ", "_"));
                    } catch (Exception e) {
                        city = IndonesianCity.JAKARTA;
                    }
                    Address address = new Address(
                            UUID.fromString(resultSet.getString("address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
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

    public static List<Address> readByUserId(Connection connection, UUID userId) throws SQLException {
        String SQL = "SELECT * FROM addresses WHERE user_id = ?";
        List<Address> addresses = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, userId.toString());
            try(var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city").toUpperCase().replaceAll(" ", "_"));
                    } catch (Exception e) {
                        continue;
                    }
                    Address address = new Address(
                            UUID.fromString(resultSet.getString("address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
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

    public static boolean update(Connection connection, UUID addressId, String receiverName, String receiverPhone, String content, String notes, IndonesianCity city) throws SQLException {
        String SQL = "UPDATE addresses SET address_receiver_name = ?, address_receiver_phone = ?, address_content = ?, address_notes = ?, address_city = ? WHERE address_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, receiverName);
            ps.setString(2, receiverPhone);
            ps.setString(3, content);
            ps.setString(4, notes);
            ps.setString(5, city.getName());
            ps.setString(6, addressId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection connection, UUID addressId) throws SQLException
    {
        String SQL = "DELETE FROM addresses WHERE address_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, addressId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public static List<Address> search(Connection connection, String query, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int offset = (page - 1) * limit;
        String SQL = "SELECT * FROM addresses WHERE address_receiver_name LIKE ? OR address_receiver_phone LIKE ? OR address_content LIKE ? OR address_notes LIKE ? OR address_city LIKE ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        List<Address> addresses = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, "%" + query + "%");
            ps.setString(5, "%" + query + "%");
            ps.setInt(6, limit);
            ps.setInt(7, offset);
            try(var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    IndonesianCity city;
                    try {
                        city = IndonesianCity.valueOf(resultSet.getString("address_city"));
                    } catch (Exception e) {
                        city = IndonesianCity.JAKARTA;
                    }
                    Address address = new Address(
                            UUID.fromString(resultSet.getString("address_id")),
                            UUID.fromString(resultSet.getString("user_id")),
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
