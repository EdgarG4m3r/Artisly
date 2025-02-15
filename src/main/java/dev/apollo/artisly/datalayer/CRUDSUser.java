package dev.apollo.artisly.datalayer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.pagination.PaginatedUser;
import org.checkerframework.checker.units.qual.C;
import redis.clients.jedis.Jedis;

import javax.swing.text.html.Option;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CRUDSUser {


    public static User create(Connection connection, String email, String password, String firstName, String lastName, String phoneNumber) throws SQLException {
        String query = "INSERT INTO users (user_id, user_email, user_password, user_first_name, user_last_name, user_phone_number, user_banned, user_created, user_updated, user_email_verified, nomor_ktp, admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.now();
            ps.setString(1, userId.toString());
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, firstName);
            ps.setString(5, lastName);
            ps.setString(6, phoneNumber);
            ps.setBoolean(7, false);
            ps.setDate(8, Date.valueOf(date));
            ps.setDate(9, Date.valueOf(date));
            ps.setDate(10, null);
            ps.setString(11, null);
            ps.setBoolean(12, false);
            ps.executeUpdate();
            User user = new User(userId, email, password, firstName, lastName, phoneNumber, false, date, date, Optional.empty(), Optional.empty(), false);
            addToCache(user);
            return user;
        }
    }

    public static Optional<User> readById(Connection connection, UUID userId) throws SQLException {
        Optional<User> cachedUser = getFromCacheById(userId);
        if (cachedUser.isPresent())
        {
            return cachedUser;
        }
        String query = "SELECT * FROM users WHERE user_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1, userId.toString());
            try(ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    addToCache(user);
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<User> readByEmail(Connection connection, String email) throws SQLException {
        Optional<User> cachedUser = getFromCacheByEmail(email);
        if (cachedUser.isPresent())
        {
            return cachedUser;
        }
        String query = "SELECT * FROM users WHERE user_email = ?";
        try(PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    addToCache(user);
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<User> readByPhoneNumber(Connection connection, String phoneNumber) throws SQLException {
        Optional<User> cachedUser = getFromCacheByPhoneNumber(phoneNumber);
        if (cachedUser.isPresent())
        {
            return cachedUser;
        }
        String query = "SELECT * FROM users WHERE user_phone_number = ?";
        try(PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1, phoneNumber);
            try(ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    addToCache(user);
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public static PaginatedUser readAll(Connection connection, int page, int limit, String sort_by, boolean ascending) throws SQLException {
        int total = count(connection);
        int offset = (page - 1) * limit;
        String query = "SELECT * FROM users ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";

        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try(ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    users.add(user);
                }
                return new PaginatedUser(users, sort_by, ascending, page, limit, (int) Math.ceil((double) total / limit), total);
            }
        }
    }

    public static List<User> readAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM users";
        try(PreparedStatement ps = connection.prepareStatement(query))
        {
            try(ResultSet rs = ps.executeQuery())
            {
                List<User> users = new ArrayList<>();
                while (rs.next())
                {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    users.add(user);
                }
                return users;
            }
        }
    }
    public static PaginatedUser search(Connection connection, String query, int page, int limit, String sort_by, boolean ascending) throws SQLException
    {
        int count = count(connection, query);
        int offset = (page - 1) * limit;
        String sql = "SELECT * FROM users WHERE user_email LIKE ? OR user_first_name LIKE ? OR user_last_name LIKE ? OR user_phone_number LIKE ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ps.setString(4, "%" + query + "%");
            ps.setInt(5, limit);
            ps.setInt(6, offset);
            try(ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_first_name"),
                            rs.getString("user_last_name"),
                            rs.getString("user_phone_number"),
                            rs.getBoolean("user_banned"),
                            rs.getDate("user_created").toLocalDate(),
                            rs.getDate("user_updated").toLocalDate(),
                            rs.getDate("user_email_verified") == null ? Optional.empty() : Optional.of(rs.getDate("user_email_verified").toLocalDate()),
                            Optional.ofNullable(rs.getString("nomor_ktp")),
                            rs.getBoolean("admin")
                    );
                    users.add(user);
                }
                return new PaginatedUser(users, sort_by, ascending, page, limit, (int) Math.ceil((double) count / limit), count);
            }
        }
    }

    public static boolean update(Connection connection, UUID userId, String userEmail, String userPassword, String userFirstName, String userLastName, String userPhoneNumber, boolean userBanned, LocalDate userCreated, LocalDate userUpdated, Optional<LocalDate> userEmailVerified, Optional<String> nomorKTP, boolean admin) throws SQLException {
        String query = "UPDATE users SET user_email = ?, user_password = ?, user_first_name = ?, user_last_name = ?, user_phone_number = ?, user_banned = ?, user_created = ?, user_updated = ?, user_email_verified = ?, nomor_ktp = ?, admin = ? WHERE user_id = ?";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userEmail);
            ps.setString(2, userPassword);
            ps.setString(3, userFirstName);
            ps.setString(4, userLastName);
            ps.setString(5, userPhoneNumber);
            ps.setBoolean(6, userBanned);
            ps.setDate(7, Date.valueOf(userCreated));
            ps.setDate(8, Date.valueOf(userUpdated));
            if(userEmailVerified.isPresent()) {
                ps.setDate(9, Date.valueOf(userEmailVerified.get()));
            } else {
                ps.setNull(9, Types.DATE);
            }
            ps.setString(10, nomorKTP.orElse(null));
            ps.setBoolean(11, admin);
            ps.setString(12, userId.toString());
            removeFromCache(new User(userId, userEmail, userPassword, userFirstName, userLastName, userPhoneNumber, userBanned, userCreated, userUpdated, userEmailVerified, nomorKTP, admin));
            return ps.executeUpdate() > 0;
        }
    }


    public static int count(Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) FROM users";
        try(PreparedStatement PreparedStatement = connection.prepareStatement(query)) {
            try(ResultSet rs = PreparedStatement.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int count(Connection connection, String search) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE user_email LIKE ? OR user_first_name LIKE ? OR user_last_name LIKE ? OR user_phone_number LIKE ?";
        try(PreparedStatement PreparedStatement = connection.prepareStatement(query)) {
            PreparedStatement.setString(1, "%" + search + "%");
            PreparedStatement.setString(2, "%" + search + "%");
            PreparedStatement.setString(3, "%" + search + "%");
            PreparedStatement.setString(4, "%" + search + "%");
            try(ResultSet rs = PreparedStatement.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private static void removeFromCache(User user)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            jedis.del("user-by-id:" + user.id());
            jedis.del("user-by-email:" + user.email());
            jedis.del("user-by-phone-number:" + user.phoneNumber());
        }
    }

    private static void addToCache(User user)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            String serialized = user.serialize();
            jedis.setex("user-by-id:" + user.id(), 60 * 60 * 24, serialized);
            jedis.setex("user-by-email:" + user.email(), 60 * 60 * 24, serialized);
            jedis.setex("user-by-phone-number:" + user.phoneNumber(), 60 * 60 * 24, serialized);
        }
    }

    private static Optional<User> getFromCacheById(UUID uuid)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            String serialized = jedis.get("user-by-id:" + uuid);
            if(serialized != null)
            {
                return Optional.of(User.deserialize(serialized));
            }
        }
        return Optional.empty();
    }

    private static Optional<User> getFromCacheByEmail(String email)
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            String serialized = jedis.get("user-by-email:" + email);
            if(serialized != null)
            {
                return Optional.of(User.deserialize(serialized));
            }
        }
        return Optional.empty();
    }

    private static Optional<User> getFromCacheByPhoneNumber(String phoneNumber) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String serialized = jedis.get("user-by-phone-number:" + phoneNumber);
            if (serialized != null) {
                return Optional.of(User.deserialize(serialized));
            }
        }
        return Optional.empty();
    }


}
