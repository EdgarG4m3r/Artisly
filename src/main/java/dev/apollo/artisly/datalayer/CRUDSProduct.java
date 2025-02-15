package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.pagination.PaginatedProduct;
import dev.apollo.artisly.services.AlgoliaService;
import dev.apollo.artisly.services.ProductService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CRUDSProduct {

    public static Product create(Connection connection, UUID storeId, UUID categoryId, String productName, String productDescription, double productPrice, int productStock) throws SQLException {
        String SQL = "INSERT INTO products(product_id, store_id, category_id, product_name, product_description, product_price, product_stock) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(SQL))
        {
            UUID productID = UUID.randomUUID();
            ps.setString(1, productID.toString());
            ps.setString(2, storeId.toString());
            ps.setString(3, categoryId.toString());
            ps.setString(4, productName);
            ps.setString(5, productDescription);
            ps.setDouble(6, productPrice);
            ps.setInt(7, productStock);
            ps.executeUpdate();
            Product product = new Product(productID, storeId, categoryId, productName, productDescription, productStock, productPrice, LocalDate.now(), LocalDate.now());
            AlgoliaService.indexProduct(productID);
            return product;
        }
    }

    public static Optional<Product> readByProductId(Connection connection, UUID productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId.toString());
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    return Optional.of(product);
                }
            }
        }
        return Optional.empty();
    }

    public static PaginatedProduct readByCategoryId(Connection connection, UUID storeId, int page, int size, String sort_by, boolean ascending) throws SQLException
    {
        int totalProducts = getProductCountByCategoryId(connection, storeId);
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, storeId.toString());
            ps.setInt(2, size);
            ps.setInt(3, offset);
            try(ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }

    }

    public static boolean update(Connection connection, UUID productId, UUID categoryId, String productName, String productDescription, double productPrice, int productStock) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, product_name = ?, product_description = ?, product_price = ?, product_stock = ? WHERE product_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId.toString());
            ps.setString(2, productName);
            ps.setString(3, productDescription);
            ps.setDouble(4, productPrice);
            ps.setInt(5, productStock);
            ps.setString(6, productId.toString());
            int result = ps.executeUpdate();
            if (result > 0) {
                AlgoliaService.indexProduct(productId);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public static boolean delete(Connection connection, UUID productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId.toString());
            int result = ps.executeUpdate();

            if (result > 0) {
                AlgoliaService.deleteProduct(productId);
                return true;
            }
            else
            {
                return false;
            }

        }
    }

    public static PaginatedProduct searchByStore(Connection connection, UUID storeId, String query, int page, int size, String sort_by, boolean ascending) throws SQLException {
        if (query.equalsIgnoreCase("%"))
        {
            return readByStoreId(connection, storeId, page, size, sort_by, ascending);
        }

        int totalProducts = getProductCountByStoreId(connection, storeId, query);
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;

        String sortOrder = ascending ? "ASC" : "DESC";

        String sortByColumn;
        switch (sort_by) {
            case "review":
                sortByColumn = "avg_review";
                break;
            case "price":
                sortByColumn = "p.product_price";
                break;
            default:
                sortByColumn = "p.product_created";
        }


        String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, AVG(r.review_rating) AS avg_review " +
                "FROM products p " +
                "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                "WHERE p.store_id = ? " +
                "AND (p.product_name LIKE ? OR p.product_description LIKE ?) " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";

        try(PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, storeId.toString());
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, sortByColumn);
            ps.setString(5, sortOrder);
            ps.setInt(6, size);
            ps.setInt(7, offset);
            try(ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                System.out.println("Total Products: " + totalProducts);
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }

    }

    public static PaginatedProduct search(Connection connection, String query, int page, int size, String sort_by, boolean ascending) throws SQLException {

        if (query.equalsIgnoreCase("%"))
        {
            return readAll(connection, page, size, sort_by, ascending);
        }

        int totalProducts = getProductCount(connection, query);
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;

        String sortOrder = ascending ? "ASC" : "DESC";

        String sortByColumn;
        switch (sort_by) {
            case "review":
                sortByColumn = "avg_review";
                break;
            case "price":
                sortByColumn = "p.product_price";
                break;
            default:
                sortByColumn = "p.product_created";
        }

        String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                "WHERE p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ? " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, "%" + query + "%");
            ps.setInt(5, size);
            ps.setInt(6, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    System.out.println("Creating product from search : " + rs.getString("product_name"));
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }
    }

    //page, size, sort_by, ascending);
    public static PaginatedProduct readAll(Connection connection, int page, int size, String sort_by, boolean ascending) throws SQLException
    {
        int totalProducts = getProductCount(connection);
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;

        String sortOrder = ascending ? "ASC" : "DESC";

        String sortByColumn;

        switch (sort_by) {
            case "review":
                sortByColumn = "avg_review";
                break;
            case "price":
                sortByColumn = "p.product_price";
                break;
            default:
                sortByColumn = "p.product_created";
        }

        /*String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN reviews r ON p.product_id = r.product_id " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";*/

        String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }
    }

    public static PaginatedProduct readByStoreId(Connection connection, UUID storeId, int page, int size, String sort_by, boolean ascending) throws SQLException
    {
        int totalProducts = getProductCountByStoreId(connection, storeId, "%");
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;

        String sortOrder = ascending ? "ASC" : "DESC";

        String sortByColumn;

        switch (sort_by) {
            case "review":
                sortByColumn = "avg_review";
                break;
            case "price":
                sortByColumn = "p.product_price";
                break;
            default:
                sortByColumn = "p.product_created";
        }

        /*String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN reviews r ON p.product_id = r.product_id " +
                "WHERE p.store_id = ? " +
                "GROUP BY p.product_id " +
                "ORDER BY ? ? " +
                "LIMIT ? OFFSET ?";*/

        String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                "WHERE p.store_id = ? " +
                "GROUP BY p.product_id " +
                "ORDER BY ? ? " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, storeId.toString());
            ps.setString(2, sortByColumn);
            ps.setString(3, sortOrder);
            ps.setInt(4, size);
            ps.setInt(5, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }
    }


    public static PaginatedProduct searchPriority(Connection connection, String query, int page, int size, String sort_by, boolean ascending) throws SQLException {
        int totalProducts = getProductCountPriority(connection, query);
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        int offset = (page - 1) * size;

        String sortOrder = ascending ? "ASC" : "DESC";

        String sortByColumn;
        switch (sort_by) {
            case "review":
                sortByColumn = "avg_review";
                break;
            case "price":
                sortByColumn = "p.product_price";
                break;
            default:
                sortByColumn = "p.product_created";
        }

        /*String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN reviews r ON p.product_id = r.product_id " +
                "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_vet_id IS NOT NULL " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";*/

        String SQL = "SELECT p.product_id, p.store_id, p.category_id, p.product_name, p.product_description, p.product_stock, p.product_price, p.product_created, p.product_updated, c.category_name, s.store_name, AVG(r.review_rating) as avg_review " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_vet_id IS NOT NULL " +
                "GROUP BY p.product_id " +
                "ORDER BY " + sortByColumn + " " + sortOrder + " " +
                "LIMIT ? OFFSET ?";


        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, "%" + query + "%");
            ps.setInt(5, size);
            ps.setInt(6, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                            UUID.fromString(rs.getString("product_id")),
                            UUID.fromString(rs.getString("store_id")),
                            UUID.fromString(rs.getString("category_id")),
                            rs.getString("product_name"),
                            rs.getString("product_description"),
                            rs.getInt("product_stock"),
                            rs.getDouble("product_price"),
                            rs.getDate("product_created").toLocalDate(),
                            rs.getDate("product_updated").toLocalDate()
                    );
                    products.add(product);
                }
                return new PaginatedProduct(products, sort_by, ascending, page, size, totalPages, totalProducts);
            }
        }
    }


    public static int getProductCountByStoreId(Connection connection, UUID storeId, String query) throws SQLException {
        String sql;
        /*if (query.equalsIgnoreCase("%")) {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " + // Added space before WHERE
                    "WHERE s.store_id = ?";
        } else {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN reviews r ON p.product_id = r.product_id " +
                    "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_id = ?";
        }*/

        if (query.equalsIgnoreCase("%")) {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "WHERE s.store_id = ?";
        } else {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                    "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                    "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                    "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_id = ?";
        }


        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!query.equalsIgnoreCase("%")) {
                ps.setString(1, "%" + query + "%");
                ps.setString(2, "%" + query + "%");
                ps.setString(3, "%" + query + "%");
                ps.setString(4, "%" + query + "%");
                ps.setString(5, storeId.toString());
            } else {
                ps.setString(1, storeId.toString());
            }
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }


    public static int getProductCountByCategoryId(Connection connection, UUID categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try(var ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId.toString());
            try(var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int getProductCount(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int getProductCount(Connection connection, String query) throws SQLException {
        String sql;
        if (query.equalsIgnoreCase("%")) {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id";
        } else {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                    "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                    "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                    "WHERE p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?";
        }


        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!query.equalsIgnoreCase("%")) {
                ps.setString(1, "%" + query + "%");
                ps.setString(2, "%" + query + "%");
                ps.setString(3, "%" + query + "%");
                ps.setString(4, "%" + query + "%");
            }
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int getProductCountPriority(Connection connection, String query) throws SQLException {
        String sql;
        if (query.equalsIgnoreCase("%")) {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "WHERE s.store_vet_id IS NOT NULL";
        } else {
            sql = "SELECT COUNT(DISTINCT p.product_id) FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                    "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                    "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                    "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_vet_id IS NOT NULL";
        }


        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!query.equalsIgnoreCase("%")) {
                ps.setString(1, "%" + query + "%");
                ps.setString(2, "%" + query + "%");
                ps.setString(3, "%" + query + "%");
                ps.setString(4, "%" + query + "%");
            }
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
    public static int countRecommended(Connection connection, String query, List<UUID> productsId) throws SQLException {
        String sql;
        if (query.equalsIgnoreCase("%")) {
            sql = "SELECT COUNT(DISTINCT p.product_id), FIELD(p.product_id, " +
                    String.join(", ", productsId.stream().map(UUID::toString).collect(Collectors.toList())) +
                    ") as priority FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "WHERE s.store_vet_id IS NOT NULL ORDER BY priority DESC";
        } else {
            sql = "SELECT COUNT(DISTINCT p.product_id), FIELD(p.product_id, " +
                    String.join(", ", productsId.stream().map(UUID::toString).collect(Collectors.toList())) +
                    ") as priority FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN immutable_products ip ON p.product_id = ip.product_id " +
                    "LEFT JOIN orders o ON ip.immutable_product_id = o.copy_of_product_id " +
                    "LEFT JOIN reviews r ON o.order_id = r.order_id " +
                    "WHERE (p.product_name LIKE ? OR p.product_description LIKE ? OR c.category_name LIKE ? OR s.store_name LIKE ?) AND s.store_vet_id IS NOT NULL" +
                    "ORDER BY priority DESC";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!query.equalsIgnoreCase("%")) {
                ps.setString(1, "%" + query + "%");
                ps.setString(2, "%" + query + "%");
                ps.setString(3, "%" + query + "%");
                ps.setString(4, "%" + query + "%");
            }
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;

    }

}
