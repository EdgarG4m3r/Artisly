package dev.apollo.artisly.security;


import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class HashEngine {

    // Parameters for Argon2 hashing algorithm
    // Argon2id resist brute-force and side channel attacks better
    // DO NOT CHANGE THESE VALUES ONCE THE SYSTEM IS IN PRODUCTION
    private static Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id,
            32,
            64);


    /**
     * Hash a string using SHA-256
     * This method is used to hash user's password
     * @param password
     * @return
     */
    public static String hashPassword(char[] password) {
        //Balance between security and performance since this function may be abused by attackers
        return argon2.hash(1, 32768, 4, password);
    }

    /**
     * Verify a password against a hash
     * This method is used in authentication process where the user enters a password and the hash is retrieved from the database
     * @param hash      The hash to verify against
     * @param password  The password to verify
     * @return          True if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String hash, char[] password) {
        return argon2.verify(hash, password);
    }


    /**
     * Generate a random password
     * @return  Random password in char array format
     */
    public static char[] randomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt).toCharArray();
    }

    /**
     * Generate a random 8 digit OTP code
     * @return  8 digit OTP code in string format
     */
    public static String generateVerificationCode()
    {
        SecureRandom random = new SecureRandom();
        int[] digits = new int[8];
        for (int i = 0; i < 8; i++) {
            digits[i] = random.nextInt(9);
        }
        return String.format("%d%d%d%d%d%d%d%d", digits[0], digits[1], digits[2], digits[3], digits[4], digits[5], digits[6], digits[7]);
    }

}
