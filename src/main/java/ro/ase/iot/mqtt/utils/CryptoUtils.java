package ro.ase.iot.mqtt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.ase.iot.mqtt.model.TelemetryData;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {
    private static final String ENCRYPTION_MODE = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH = 12;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String encrypt(String plaintext, byte[] key) {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedBase64, byte[] key) {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);
        byte[] iv = new byte[IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - iv.length];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptTelemetry(TelemetryData data, byte[] key) {
        try {
            String json = mapper.writeValueAsString(data);
            return encrypt(json, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize and encrypt telemetry", e);
        }
    }

    public static <T> T decryptTelemetry(String encryptedBase64, byte[] key, Class<T> type) {
        try {
            String json = decrypt(encryptedBase64, key);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt or deserialize JSON telemetry", e);
        }
    }
}
