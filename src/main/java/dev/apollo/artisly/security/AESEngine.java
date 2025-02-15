package dev.apollo.artisly.security;

import dev.apollo.artisly.Artisly;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESEngine {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256; // AES 256. Valid values are 128, 192, 256
    private SecretKey secretKey;
    boolean error = false;

    public AESEngine(String pepper) {
        try {
            secretKey = deriveSecretKey(pepper);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Artisly.getLogger().error("Error while generating secret key. AESEngine is fault-mode", e);
            error = true;
        }
    }

    public String encrypt(String strToEncrypt) throws NullPointerException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        if (error)
            throw new NullPointerException("Error while generating secret key. AESEngine is fault-mode");

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // AES GCM helps resist tampering of the encrypted data.
        // Useful when the ciphertext is stored in a location where an attacker could modify it.
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);
        byte[] cipherMessage = byteBuffer.array();

        return Base64.getEncoder().encodeToString(cipherMessage);
    }

    public String decrypt(String strToDecrypt) throws NullPointerException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        if (error)
            throw new NullPointerException("Error while generating secret key. AESEngine is fault-mode");

        byte[] cipherMessage = Base64.getDecoder().decode(strToDecrypt);
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);

        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encrypted = new byte[byteBuffer.remaining()];
        byteBuffer.get(encrypted);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Derive a {@link SecretKey} from a key string. The key string is used as a password to derive the key.
     * The {@link SecretKey} is used for encryption and decryption of session tokens.
     * @param key                           The key string
     * @return                              The derived {@link SecretKey}
     * @throws NoSuchAlgorithmException     If the algorithm is not available
     * @throws InvalidKeySpecException      If the key specification is invalid
     */
    private SecretKey deriveSecretKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), key.getBytes(StandardCharsets.UTF_8), 65536, KEY_LENGTH);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public boolean isFault() {
        return error;
    }
}
