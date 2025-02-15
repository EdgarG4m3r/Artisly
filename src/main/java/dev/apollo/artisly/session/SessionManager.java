package dev.apollo.artisly.session;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.security.AESEngine;
import dev.apollo.artisly.session.exception.InvalidTokenException;
import dev.apollo.artisly.session.exception.MissingTokenException;
import dev.apollo.artisly.session.exception.TokenSigningException;
import dev.apollo.artisly.session.exception.UnsignedTokenException;
import redis.clients.jedis.Jedis;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class SessionManager {

    public static final String tokenRegex = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$";
    private AESEngine aesEngine;
    private Artisly artisly;

    public SessionManager(Artisly artisly) {
        this.artisly = artisly;
        this.aesEngine = new AESEngine(artisly.getConfiguration().session_pepper);
    }

    private boolean isTokenValid(String key)
    {
        //the key is tokenData which consist of user_id:expiry:random
        String[] tokenData = key.split(":");
        UUID user_id = UUID.fromString(tokenData[0]);
        long expiry = Long.parseLong(tokenData[1]);
        String random = tokenData[2];

        if (System.currentTimeMillis() > expiry) {
            return false;
        }

        try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
            if (jedis.sismember("token_blacklist", key)) {
                return false;
            }
        }
        return true;
    }


    public String generateToken(UUID user_id) throws TokenSigningException
    {
        try
        {
            String randomData = UUID.randomUUID().toString();
            String expiry = String.valueOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
            String tokenData = user_id.toString() + ":" + expiry + ":" + randomData;
            String signedToken = aesEngine.encrypt(tokenData);

            //check if user has a token already, if user has 5 tokens, remove the oldest one
            try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
                Set<String> tokens = jedis.smembers("user_tokens:" + user_id.toString());
                if (tokens.size() >= 5) {
                    jedis.srem("user_tokens:" + user_id.toString(), tokens.iterator().next());
                }
                jedis.sadd("user_tokens:" + user_id.toString(), signedToken);
            }

            //add token to the user set in redis
            try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
                jedis.sadd("user_tokens:" + user_id.toString(), signedToken);
                jedis.expire("user_tokens:" + user_id.toString(), (int) TimeUnit.DAYS.toSeconds(1));
            }

            return signedToken;
        }
        catch (Exception e)
        {
            throw new TokenSigningException();
        }

    }

    public UUID validateToken(String token) throws InvalidTokenException, MissingTokenException, UnsignedTokenException {
        if (token == null) {
            throw new MissingTokenException();
        }
        if (!token.matches(tokenRegex)) {
            throw new InvalidTokenException();
        }
        if (token.isEmpty() || token.equals("null")) {
            throw new MissingTokenException();
        }
        long currentTime = System.nanoTime();
        try {
            String tokenData = aesEngine.decrypt(token);
            String[] split = tokenData.split(":");
            UUID user_id = UUID.fromString(split[0]);
            long expiry = Long.parseLong(split[1]);
            String random = split[2];

            if (System.currentTimeMillis() > expiry) {
                throw new InvalidTokenException();
            }

            if (!isTokenValid(tokenData)) {
                throw new InvalidTokenException();
            }

            return user_id;

        } catch (NullPointerException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new UnsignedTokenException();
        }
    }

    public void invalidateSession(String token) throws InvalidTokenException, MissingTokenException, UnsignedTokenException {
        if (token == null) {
            throw new MissingTokenException();
        }
        if (!token.matches(tokenRegex)) {
            throw new InvalidTokenException();
        }
        if (token.isEmpty() || token.equals("null")) {
            throw new MissingTokenException();
        }
        try {
            String tokenData = aesEngine.decrypt(token);
            String[] split = tokenData.split(":");
            UUID user_id = UUID.fromString(split[0]);
            long expiry = Long.parseLong(split[1]);
            String random = split[2];

            if (System.currentTimeMillis() > expiry) {
                throw new InvalidTokenException();
            }

            try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
                jedis.sadd("token_blacklist", tokenData);
                jedis.expire("token_blacklist", (int) TimeUnit.DAYS.toSeconds(2));
                jedis.srem("user_tokens:" + user_id.toString(), token);
            }

        } catch (Exception e) {
            throw new UnsignedTokenException();
        }
    }

    public void invalidateAllSessions(UUID user_id) {
        try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
            Set<String> tokens = jedis.smembers("user_tokens:" + user_id.toString());
            for (String token : tokens) {
                jedis.sadd("token_blacklist", token);
            }
            jedis.expire("token_blacklist", (int) TimeUnit.DAYS.toSeconds(2));
            jedis.del("user_tokens:" + user_id.toString());
        }
    }

    public void invalidateAllSessions(UUID user_id, String except) {
        try (Jedis jedis = artisly.getRedis().getJedis().getResource()) {
            Set<String> tokens = jedis.smembers("user_tokens:" + user_id.toString());
            for (String token : tokens) {
                if (!token.equals(except)) {
                    jedis.sadd("token_blacklist", token);
                }
            }
            jedis.expire("token_blacklist", (int) TimeUnit.DAYS.toSeconds(2));
        }
    }

}
