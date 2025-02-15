package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.Order;
import dev.apollo.artisly.models.OrderStatus;
import dev.apollo.artisly.models.pagination.PaginatedOrder;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CRUDSOrder {

    public static Order create(Connection connection, UUID storeId, UUID userId, UUID immuteableProduct, int productQuantity, double orderPrice, UUID immuteableAddress, OrderStatus orderStatus) throws SQLException {
        String SQL = "INSERT INTO orders (order_id, store_id, user_id, copy_of_product_id, product_quantity, order_price, copy_of_address_id, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(var ps = connection.prepareStatement(SQL)) {
            UUID orderId = UUID.randomUUID();
            ps.setString(1, orderId.toString());
            ps.setString(2, storeId.toString());
            ps.setString(3, userId.toString());
            ps.setString(4, immuteableProduct.toString());
            ps.setInt(5, productQuantity);
            ps.setDouble(6, orderPrice);
            ps.setString(7, immuteableAddress.toString());
            ps.setString(8, orderStatus.toString());
            ps.executeUpdate();
            return new Order(orderId, storeId, userId, immuteableProduct, productQuantity, orderPrice, immuteableAddress, orderStatus);
        }
    }

    public static Optional<Order> readByOrderId(Connection connection, UUID orderId) throws SQLException {
        String SQL = "SELECT * FROM orders WHERE order_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, orderId.toString());
            try(var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    OrderStatus orderStatus;
                    try {
                        orderStatus = OrderStatus.valueOf(resultSet.getString("order_status"));
                    } catch (Exception e) {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(resultSet.getString("order_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("copy_of_product_id")),
                            resultSet.getInt("product_quantity"),
                            resultSet.getDouble("order_price"),
                            UUID.fromString(resultSet.getString("copy_of_address_id")),
                            orderStatus
                    );
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public static void updateOrderStatus(Connection connection, UUID orderId, OrderStatus orderStatus) throws SQLException {
        String SQL = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, orderStatus.toString());
            ps.setString(2, orderId.toString());
            ps.executeUpdate();
        }
    }

    public static void deleteByOrderId(Connection connection, UUID orderId) throws SQLException {
        String SQL = "DELETE FROM orders WHERE order_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, orderId.toString());
            ps.executeUpdate();
        }
    }

    public static PaginatedOrder readByUserId(Connection connection, UUID userId, int page, int pageSize) throws SQLException
    {
        int totalOrders = countByUserId(connection, userId);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        String SQL = "SELECT * FROM orders WHERE user_id = ? LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, userId.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            List<Order> orders = new ArrayList<>();
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    OrderStatus orderStatus;
                    try {
                        orderStatus = OrderStatus.valueOf(resultSet.getString("order_status"));
                    } catch (Exception e) {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(resultSet.getString("order_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("copy_of_product_id")),
                            resultSet.getInt("product_quantity"),
                            resultSet.getDouble("order_price"),
                            UUID.fromString(resultSet.getString("copy_of_address_id")),
                            orderStatus
                    );
                    orders.add(order);
                }
                return new PaginatedOrder(orders, "created", true, page, pageSize, totalPages, totalOrders);
            }

        }
    }

    public static PaginatedOrder readByStoreId(Connection connection, UUID storeId, int page, int pageSize) throws SQLException
    {
        int totalOrders = countByStoreId(connection, storeId);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        String SQL = "SELECT * FROM orders WHERE store_id = ?" + " LIMIT ? OFFSET ?";
        try (var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, storeId.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            List<Order> orders = new ArrayList<>();
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    OrderStatus orderStatus;
                    try {
                        orderStatus = OrderStatus.valueOf(resultSet.getString("order_status"));
                    } catch (Exception e) {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(resultSet.getString("order_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("copy_of_product_id")),
                            resultSet.getInt("product_quantity"),
                            resultSet.getDouble("order_price"),
                            UUID.fromString(resultSet.getString("copy_of_address_id")),
                            orderStatus
                    );
                    orders.add(order);
                }
                return new PaginatedOrder(orders, "created", true, page, pageSize, totalPages, totalOrders);
            }

        }
    }

    public static PaginatedOrder readByProductId(Connection connection, UUID productId, int page, int pageSize, String sort_by, boolean ascending) throws SQLException
    {
        int totalOrders = countByProductId(connection, productId);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        /*
        Be careful, this query might be vulnerable to SQL injection. Please ensure that the sort_by parameter is properly validated using InputFilter.
         */
        String SQL = "SELECT * FROM orders JOIN immutable_products ON orders.copy_of_product_id = immutable_products.immutable_product_id WHERE immutable_products.product_id = ? ORDER BY immutable_products." + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";

        try(PreparedStatement ps = connection.prepareStatement(SQL))
        {
            ps.setString(1, productId.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);

            try(ResultSet rs = ps.executeQuery())
            {
                List<Order> orders = new ArrayList<>();
                while(rs.next())
                {
                    OrderStatus orderStatus;
                    try
                    {
                        orderStatus = OrderStatus.valueOf(rs.getString("order_status"));
                    }
                    catch(Exception e)
                    {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(rs.getString("order_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("user_id")),
                            UUID.fromString(rs.getString("copy_of_product_id")),
                            rs.getInt("product_quantity"),
                            rs.getDouble("order_price"),
                            UUID.fromString(rs.getString("copy_of_address_id")),
                            orderStatus
                    );
                    orders.add(order);
                }
                return new PaginatedOrder(orders, sort_by, ascending, page, pageSize, totalPages, totalOrders);
            }
        }
    }

    public static int countByUserId(Connection connection, UUID userId) throws SQLException
    {
        String SQL = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, userId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static List<Order> readAll(Connection connection) throws SQLException
    {
        String SQL = "SELECT * FROM orders";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (resultSet.next()) {
                    OrderStatus orderStatus;
                    try {
                        orderStatus = OrderStatus.valueOf(resultSet.getString("order_status"));
                    } catch (Exception e) {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(resultSet.getString("order_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("copy_of_product_id")),
                            resultSet.getInt("product_quantity"),
                            resultSet.getDouble("order_price"),
                            UUID.fromString(resultSet.getString("copy_of_address_id")),
                            orderStatus
                    );
                    orders.add(order);
                }
                return orders;
            }
        }
    }

    public static List<Order> readAll(Connection connection, String range) throws SQLException
    {
        LocalDate startDate = getStartDate(range);
        //String SQL = "SELECT * FROM orders JOIN order_records ON orders.order_id = order_records.order_id WHERE order_records.date >= ?";
        String SQL = "SELECT * FROM orders WHERE created >= ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setDate(1, Date.valueOf(startDate));
            try (ResultSet resultSet = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (resultSet.next()) {
                    OrderStatus orderStatus;
                    try {
                        orderStatus = OrderStatus.valueOf(resultSet.getString("order_status"));
                    } catch (Exception e) {
                        orderStatus = OrderStatus.CREATED;
                    }
                    Order order = new Order(
                            UUID.fromString(resultSet.getString("order_id")),
                            UUID.fromString(resultSet.getString("store_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            UUID.fromString(resultSet.getString("copy_of_product_id")),
                            resultSet.getInt("product_quantity"),
                            resultSet.getDouble("order_price"),
                            UUID.fromString(resultSet.getString("copy_of_address_id")),
                            orderStatus
                    );
                    orders.add(order);
                }
                return orders;
            }
        }
    }

    public static int countByStoreId(Connection connection, UUID storeId) throws SQLException
    {
        String SQL = "SELECT COUNT(*) FROM orders WHERE store_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, storeId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int countByProductId(Connection connection, UUID productId) throws SQLException
    {
        String SQL = "SELECT COUNT(*) FROM orders JOIN immutable_products ON orders.copy_of_product_id = immutable_products.immutable_product_id WHERE immutable_products.product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, productId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private static LocalDate getStartDate(String range)
    {
        LocalDate startDate;
        switch(range)
        {
            case "last_week":
                startDate = LocalDate.now().minusDays(7);
                break;
            case "last_month":
                startDate = LocalDate.now().minusMonths(1);
                break;
            case "last_year":
                startDate = LocalDate.now().minusYears(1);
                break;
            default:
                startDate = LocalDate.now();
                break;
        }
        return startDate;
    }

}
