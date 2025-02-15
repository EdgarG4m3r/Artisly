package dev.apollo.artisly.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.*;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.models.pagination.PaginatedUser;
import dev.apollo.artisly.security.HashEngine;
import dev.apollo.artisly.session.SessionManager;
import dev.apollo.artisly.session.exception.InvalidTokenException;
import dev.apollo.artisly.session.exception.MissingTokenException;
import dev.apollo.artisly.session.exception.TokenSigningException;
import dev.apollo.artisly.session.exception.UnsignedTokenException;
import org.apache.tika.Tika;
import org.checkerframework.checker.units.qual.A;
import redis.clients.jedis.Jedis;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserService {

    /**
     * Used by USER to login
     * @param email
     * @param password
     * @return
     * @throws SQLException
     * @throws UserNotFoundException
     * @throws InvalidCredentialsException
     * @throws UserBannedException
     */
    public static UserSessionContainer login(String ip, String email, char[] password) throws SQLException, UserNotFoundException, InvalidCredentialsException, EmailNotVerifiedException, UserBannedException, TokenSigningException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("Tidak dapat menemukan user dengan email " + email + "!");
            }
            User user = optionalUser.get();
            if (user.banned())
            {
                throw new UserBannedException("Akun anda telah dibanned!");
            }

            try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
            {
                int attempt;
                String key = "login_attempt:" + user.id();
                String attempts = jedis.get(key);
                if (attempts == null)
                {
                    attempt = 1;
                }
                else
                {
                    attempt = Integer.parseInt(attempts);
                }

                if (attempt >= 5)
                {
                    throw new UserBannedException("Percobaan login diblokir sementara! Silahkan coba lagi dalam 1 menit.");
                }
            }
            if (!HashEngine.verifyPassword(user.password(), password))
            {
                try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
                {
                    String key = "login_attempt:" + user.id();
                    String attempts = jedis.get(key);
                    if (attempts == null)
                    {
                        jedis.set(key, "1");
                        jedis.expire(key, 60);
                    }
                    else
                    {
                        int attemptCount = Integer.parseInt(attempts);
                        if (attemptCount >= 5)
                        {
                            throw new UserBannedException("Percobaan login diblokir sementara! Silahkan coba lagi dalam 1 menit.");
                        }
                        jedis.incr(key);
                    }
                }
                throw new InvalidCredentialsException("Password yang anda masukkan salah!");
            }
            if (user.emailVerified().isEmpty())
            {
                throw new EmailNotVerifiedException("Akun anda belum diverifikasi! Silahkan cek email anda untuk melakukan verifikasi.");
            }

            try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
            {
                String key = "user_ip:" + user.id() + ":" + ip;
                if (!jedis.exists(key))
                {
                    String verificationCode;// = HashEngine.generateVerificationCode();
                    if (jedis.exists("ip_caches:" + ip))
                    {
                        throw new InvalidCredentialsException("Kami sudah mengirimkan email verifikasi ke alamat email anda, silahkan cek email anda untuk melakukan verifikasi. Jika belum menerima email, silahkan coba lagi dalam " + jedis.ttl("ip_caches:" + ip) + " detik.");
                    }
                    else
                    {
                        verificationCode = HashEngine.generateVerificationCode();
                        jedis.set("user_ip_awaiting_verification:" + verificationCode, user.email() + ":" + ip);
                        jedis.expire("user_ip_awaiting_verification:" + verificationCode, 60 * 5 );

                        jedis.set("ip_caches:" + ip, verificationCode);
                        jedis.expire("ip_caches:" + ip, 30);
                        //urlencode email
                        try {
                            String encodedEmail = URLEncoder.encode(user.email(), "UTF-8");
                            EmailService.queueEmail(user.email(), "Login Verification", "Kami mendeteksi bahwa anda menggunakan jaringan yang baru, silahkan akses link berikut untuk melakukan verifikasi: https://artisly.net/verify-login?email=" + encodedEmail + "&code=" + verificationCode);
                            throw new InvalidCredentialsException("Kami mendeteksi bahwa anda menggunakan jaringan yang baru, silahkan cek email anda untuk melakukan verifikasi.");
                        } catch (UnsupportedEncodingException e) {
                            throw new SQLException(e);
                        }
                    }


                }
            }

            String session = Artisly.instance.getSessionManager().generateToken(user.id());
            String formattedTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            EmailService.queueEmail(user.email(), "Login Notification", "A new successful login was detected on " + formattedTime + " from " + ip + ". If this was not you, please change your password immediately.");
            return new UserSessionContainer(session, user);

        }
    }

    /**
     * Used by USER to logout
     * @param token
     * @return
     * @throws InvalidTokenException
     * @throws UnsignedTokenException
     * @throws MissingTokenException
     */
    public static void logout(String token) throws InvalidTokenException, UnsignedTokenException, MissingTokenException {
        Artisly.instance.getSessionManager().invalidateSession(token);
    }

    public static void requestPasswordReset(String email, String phoneNumber) throws SQLException, AccountNotFoundException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
            {
                if (jedis.incr("forgot_password_attempt:" + email.toLowerCase()) > 5)
                {
                    return;
                }
                jedis.incr("forgot_password_attempt:" + email.toLowerCase());
                jedis.expire("forgot_password_attempt:" + email.toLowerCase(), 60 * 30);
            }
            Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
            if (optionalUser.isEmpty())
            {
                return;
            }

            User user = optionalUser.get();
            if (!user.phoneNumber().equalsIgnoreCase(phoneNumber))
            {
                return;
            }

            UUID verificationToken = UUID.randomUUID();

            try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
            {
                jedis.set("forgot_password:" + user.id().toString(), verificationToken.toString());
                jedis.expire("forgot_password:" + user.id().toString(), 60 * 30);

                EmailService.queueEmail(
                        user.email(),
                        "Password Reset Request",
                        "Hello " + user.firstName() + " " + user.lastName() + ", You recently requested a password reset for your account. If you don't do this, please kindly ignore this message. Please follow this link to reset your password https://artisly.net/forgotpassword/ " + user.id() + "/" + verificationToken + ", The link will expire in 30 minutes."
                );
            }
        }
    }

    public static void resetPassword(UUID userId, UUID verificationToken, char[] password) throws InvalidCredentialsException, UserNotFoundException, SQLException {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            String storedVerificationToken = jedis.get("forgot_password:" + userId.toString()).toString();
            if (storedVerificationToken.equalsIgnoreCase("nil"))
            {
                return;
            }

            if (!storedVerificationToken.equalsIgnoreCase(verificationToken.toString()))
            {
                throw new InvalidCredentialsException("Harap cek kembali link anda!");
            }
            resetPassword(userId, password);
        }
    }

    public static User register(String email, char[] password, String firstName, String lastName, String phoneNumber) throws SQLException, EmailTakenException, PhoneNumberTakenException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
            Optional<User> optionalUser2 = CRUDSUser.readByPhoneNumber(connection, phoneNumber);
            if (optionalUser.isPresent())
            {
                throw new EmailTakenException("That email is already taken");
            }
            if (optionalUser2.isPresent())
            {
                throw new PhoneNumberTakenException("That phone number is already taken");
            }

            //Uses Argon2 and encode it with UTF-8
            String hashedPassword = HashEngine.hashPassword(password);
            EmailService.queueEmail(
                    email,
                    "Registration Successful",
                    "Account registration successful, you will receive a verification code shortly! If you don't receive verification code, you can request it again by clicking the resend button on the verification page"
            );
            return CRUDSUser.create(connection, email, hashedPassword, firstName, lastName, phoneNumber);
        }
    }

    public static void registerAdmin(String email, String firstName, String lastName, String phoneNumber) throws SQLException, EmailTakenException, PhoneNumberTakenException {
        char[] password = HashEngine.randomPassword();
        register(email, password, firstName, lastName, phoneNumber);

        EmailService.queueEmail(email,
                "New Password",
                "Your account was registered on Artisly by an Administrator. Please change your password immediately. New Password : " + String.valueOf(password));

        try
        {
            requestVerificationCode(email);
        }
        catch (Exception e)
        {
            //ignored, because user can request verification code later and all of the exceptions are user related.
        }

    }

    /**
     * Used by USER to verify an email
     * @param email
     * @param code
     * @return
     * @throws SQLException
     * @throws InvalidVerificationCodeException
     * @throws UserNotFoundException
     * @throws EmailAlreadyVerified
     */
    public static boolean verifyEmail(String email, String code) throws SQLException, InvalidVerificationCodeException, UserNotFoundException, EmailAlreadyVerified
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {

            if (jedis.exists("user_ip_awaiting_verification:" + code))
            {
                String emailIp = jedis.get("user_ip_awaiting_verification:" + code);
                String emailInRedis = emailIp.split(":")[0];
                String ip = emailIp.split(":")[1];

                if (!emailInRedis.equalsIgnoreCase(email))
                {
                    throw new InvalidVerificationCodeException("Invalid verification code, if you haven't received a code yet, please request one");
                }

                jedis.del("user_ip_awaiting_verification:" + code);
                try(Connection connection = Artisly.instance.getMySQL().getConnection())
                {
                    Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
                    if (optionalUser.isEmpty())
                    {
                        throw new UserNotFoundException("We couldn't find a user with that email");
                    }
                    User user = optionalUser.get();

                    jedis.set("user_ip:" + user.id() + ":" + ip, "true");
                    jedis.expire("user_ip:" + user.id() + ":" + ip, 60 * 60 * 24 * 30);

                    EmailService.queueEmail(
                            user.email(),
                            "Trusted IP Added",
                            "IP " + ip + " has been added to your trusted IP list for 30 days"
                    );
                }
                return false;
            }
            else
            {
                if (!jedis.exists("verificationCode:" + email))
                {
                    throw new InvalidVerificationCodeException("Invalid verification code, if you haven't received a code yet, please request one");
                }
                String verificationCode = jedis.get("verificationCode:" + email);

                if (!verificationCode.equals(code))
                {
                    throw new InvalidVerificationCodeException("Invalid verification code, if you haven't received a code yet, please request one");
                }

                try(Connection connection = Artisly.instance.getMySQL().getConnection())
                {
                    connection.setAutoCommit(false);
                    jedis.del("verificationCode:" + email);
                    Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
                    if (optionalUser.isEmpty())
                    {
                        throw new UserNotFoundException("We couldn't find a user with that email");
                    }
                    User user = optionalUser.get();
                    if (user.emailVerified().isPresent())
                    {
                        throw new EmailAlreadyVerified("Your email has already been verified");
                    }
                    LocalDate date = LocalDate.now();
                    try {
                        CRUDSUser.update(connection, user.id(), user.email(), user.password(), user.firstName(), user.lastName(), user.phoneNumber(), user.banned(), user.created(), user.updated(), Optional.of(date), user.nomorKTP(), user.admin());
                    }
                    catch (SQLException e)
                    {
                        connection.rollback();
                        throw e;
                    }
                    connection.commit();
                    EmailService.queueEmail(user.email(), "Email Verification", "Thank you for verifying your email. You can now login to your account");
                    return true;
                }
            }
        }
    }

    public static String requestVerificationCode(String email) throws SQLException, UserNotFoundException, EmailAlreadyVerified, RateLimitedException
    {
        try(Jedis jedis = Artisly.instance.getRedis().getJedis().getResource())
        {
            if (jedis.exists("rateLimit:" + email)) {
                if (jedis.get("rateLimit:" + email).equals("5")) {
                    long timeLeft = jedis.ttl("rateLimit:" + email);
                    throw new RateLimitedException("Anda telah melebihi batas permintaan kode verifikasi. Silahkan coba lagi dalam " + timeLeft + " detik");
                }
                jedis.incr("rateLimit:" + email);
            } else {
                jedis.setex("rateLimit:" + email, 60, "1");
            }

            try (Connection connection = Artisly.instance.getMySQL().getConnection()) {
                Optional<User> optionalUser = CRUDSUser.readByEmail(connection, email);
                if (optionalUser.isEmpty()) {
                    throw new UserNotFoundException("Kami tidak dapat menemukan pengguna dengan email tersebut");
                }
                User user = optionalUser.get();
                if (user.emailVerified().isPresent()) {
                    throw new EmailAlreadyVerified("Email anda telah diverifikasi");
                }

                if (jedis.exists("verificationCode:" + email)) {
                    return jedis.get("verificationCode:" + email);
                } else {
                    String code = HashEngine.generateVerificationCode();
                    jedis.setex("verificationCode:" + email, 600, code);
                    EmailService.queueEmail(email, "Email Verification", "Your verification code is " + code + ". This code will expire in 10 minutes");
                    return code;
                }
            }
        }
    }

    /**
     * Used by USER to change their password
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws SQLException
     * @throws UserNotFoundException
     * @throws InvalidCredentialsException
     */
    public static boolean changePassword(UUID userId, char[] oldPassword, char[] newPassword, String currentToken) throws SQLException, UserNotFoundException, InvalidCredentialsException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readById(connection, userId);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("Kami tidak dapat menemukan pengguna dengan ID + " + userId + "! Silahkan hubungi support!");
            }
            User user = optionalUser.get();
            if (!HashEngine.verifyPassword(user.password(), oldPassword))
            {
                throw new InvalidCredentialsException("Password salah");
            }
            String hashedPassword = HashEngine.hashPassword(newPassword);
            EmailService.queueEmail(user.email(), "Password Changed", "Your password has been changed. If you did not change your password, please contact us immediately at support@artisly.net");
            boolean result = CRUDSUser.update(connection, user.id(), user.email(), hashedPassword, user.firstName(), user.lastName(), user.phoneNumber(), user.banned(), user.created(), user.updated(), user.emailVerified(), user.nomorKTP(), user.admin());
            if (result)
            {
                Artisly.instance.getSessionManager().invalidateAllSessions(userId, currentToken);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Used by ADMIN to reset a user's password
     * @param userId
     * @param newPassword
     * @return
     * @throws SQLException
     * @throws UserNotFoundException
     * @throws InvalidCredentialsException
     */
    public static boolean resetPassword(UUID userId, char[] newPassword) throws SQLException, UserNotFoundException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readById(connection, userId);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("Tidak dapat menemukan pengguna dengan ID + " + userId + "! Harap coba lagi!");
            }
            User user = optionalUser.get();
            String hashedPassword = HashEngine.hashPassword(newPassword);
            EmailService.queueEmail(user.email(), "Password Changed", "Your password has been changed. If you did not change your password, please contact us immediately at support@artisly.net");
            boolean result = CRUDSUser.update(connection, user.id(), user.email(), hashedPassword, user.firstName(), user.lastName(), user.phoneNumber(), user.banned(), user.created(), user.updated(), user.emailVerified(), user.nomorKTP(), user.admin());
            if (result)
            {
                Artisly.instance.getSessionManager().invalidateAllSessions(userId);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Used by USER to change their user
     * @param userId            User's ID (UUID)
     * @param password          User's password to verify and prevent csrf attacks
     * @param firstName         (Optional) User's first name
     * @param lastName          (Optional) User's last name
     * @param phoneNumber       (Optional) User's phone number
     * @param profilePicture    (Optional) User's user picture
     * @return
     * @throws SQLException
     * @throws UserNotFoundException
     * @throws InvalidCredentialsException
     * @throws IOException
     */
    public static boolean changeProfile(UUID userId, char[] password, Optional<String> firstName, Optional<String> lastName, Optional<String> phoneNumber, Optional<String> noKTP, Optional<InputStream> profilePicture) throws SQLException, UserNotFoundException, AccountNotVerifiedException, InvalidCredentialsException, IOException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readById(connection, userId);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("We couldn't find a user with that email");
            }
            User user = optionalUser.get();
            if (!user.nomorKTP().isEmpty())
            {
                if (noKTP.isEmpty())
                {
                    throw new AccountNotVerifiedException("No KTP tidak dapat dihapus");
                }
            }
            if (!HashEngine.verifyPassword(user.password(), password))
            {
                throw new InvalidCredentialsException("Invalid password");
            }
            if (profilePicture.isPresent())
            {
                Artisly.instance.getMediaService().uploadProfilePicture(userId, profilePicture.get(), new Tika().detect(profilePicture.get()));
            }
            return CRUDSUser.update(connection, user.id(), user.email(), user.password(), firstName.orElse(user.firstName()), lastName.orElse(user.lastName()), phoneNumber.orElse(user.phoneNumber()), user.banned(), user.created(), user.updated(), user.emailVerified(), noKTP, user.admin());
        }
    }

    public static boolean editUser(UUID userId, String firstName, String lastName, boolean banned, boolean admin) throws SQLException, UserNotFoundException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readById(connection, userId);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("Tidak dapat menemukan pengguna dengan ID + " + userId + "! Harap coba lagi!");
            }
            User user = optionalUser.get();
            //check if user is receiving a ban
            if (banned && !user.banned())
            {
                Artisly.instance.getSessionManager().invalidateAllSessions(userId);
                EmailService.queueEmail(user.email(), "Account Banned", "Your account has been banned. If you believe this is a mistake, please contact us immediately at support@artisly.net");
            }
            //check if user is getting unbanned
            else if (!banned && user.banned()) {
                EmailService.queueEmail(user.email(), "Account Unbanned", "Your account has been unbanned. You may now login again.");
            }
            return CRUDSUser.update(connection, user.id(), user.email(), user.password(), firstName, lastName, user.phoneNumber(), banned, user.created(), user.updated(), user.emailVerified(), user.nomorKTP(), admin);
        }
    }

    /**
     * Used by ADMIN for List Users
     * @param page
     * @param size
     * @param sortBy
     * @param ascending
     * @return
     * @throws SQLException
     */
    public static PaginatedUser index(int page, int size, String sortBy, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSUser.readAll(connection, page, size, sortBy, ascending);
        }
    }

    public static List<User> index() throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSUser.readAll(connection);
        }
    }
    public static PaginatedUser search(String query, int page, int size, String sortBy, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSUser.search(connection, query, page, size, sortBy, ascending);
        }
    }

    public static User show(UUID userId) throws SQLException, UserNotFoundException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> optionalUser = CRUDSUser.readById(connection, userId);
            if (optionalUser.isEmpty())
            {
                throw new UserNotFoundException("We couldn't find a user with that email");
            }
            return optionalUser.get();
        }
    }
}
