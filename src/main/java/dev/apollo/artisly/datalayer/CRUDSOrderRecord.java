package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.OrderRecord;
import dev.apollo.artisly.models.OrderStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSOrderRecord {

    //UUID orderRecordId, UUID orderId, OrderStatus orderStatus, Date date, Optional<String> note
    public static OrderRecord create(Connection connection, UUID orderId, OrderStatus orderStatus, Optional<String> note) throws SQLException
    {
        String sql = "INSERT INTO order_records (order_record_id, order_id, order_status, date, note) VALUES (?, ?, ?, ?, ?)";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            UUID orderRecordId = UUID.randomUUID();
            LocalDate date = LocalDate.now();
            PreparedStatement.setString(1, orderRecordId.toString());
            PreparedStatement.setString(2, orderId.toString());
            PreparedStatement.setString(3, orderStatus.toString());
            PreparedStatement.setDate(4, Date.valueOf(date));
            PreparedStatement.setString(5, note.orElse(null));
            PreparedStatement.executeUpdate();
            return new OrderRecord(orderRecordId, orderId, orderStatus, date, note);
        }
    }

    public static Optional<OrderRecord> readById(Connection connection, UUID orderRecordId) throws SQLException
    {
        String sql = "SELECT * FROM order_records WHERE order_record_id = ?";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            PreparedStatement.setString(1, orderRecordId.toString());
            try(var ResultSet = PreparedStatement.executeQuery())
            {
                if(ResultSet.next())
                {
                    OrderRecord orderRecord = new OrderRecord(
                            UUID.fromString(ResultSet.getString("order_record_id")),
                            UUID.fromString(ResultSet.getString("order_id")),
                            OrderStatus.valueOf(ResultSet.getString("order_status")),
                            ResultSet.getDate("date").toLocalDate(),
                            Optional.ofNullable(ResultSet.getString("note"))
                    );
                    return Optional.of(orderRecord);
                }
            }
        }
        return Optional.empty();
    }

    public static List<OrderRecord> readByOrderId(Connection connection, UUID orderId) throws SQLException
    {
        String sql = "SELECT * FROM order_records WHERE order_id = ?";
        try(var PreparedStatement = connection.prepareStatement(sql))
        {
            PreparedStatement.setString(1, orderId.toString());
            try(var ResultSet = PreparedStatement.executeQuery())
            {
                List<OrderRecord> orderRecords = new ArrayList<>();
                while(ResultSet.next())
                {
                    OrderRecord orderRecord = new OrderRecord(
                            UUID.fromString(ResultSet.getString("order_record_id")),
                            UUID.fromString(ResultSet.getString("order_id")),
                            OrderStatus.valueOf(ResultSet.getString("order_status")),
                            ResultSet.getDate("date").toLocalDate(),
                            Optional.ofNullable(ResultSet.getString("note"))
                    );
                    orderRecords.add(orderRecord);
                }
                return orderRecords;
            }
        }
    }

}
