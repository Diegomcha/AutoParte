package me.diegomcha.autoparte.core.security;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.diegomcha.autoparte.config.AutoparteProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

@Converter
@Component
public class PersistenceEncryptionConverter implements AttributeConverter<Object, byte[]> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Random secureRandom = new SecureRandom();
    private final Key key;
    private final ObjectMapper objectMapper;

    public PersistenceEncryptionConverter(AutoparteProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        var keyBytes = properties.getSecurity().getDbEncryptionKey().getBytes(CHARSET);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Invalid AES key length: " + keyBytes.length + " bytes. " +
                            "The property 'autoparte.security.db-encryption-key' must be exactly 16, 24, or 32 characters long."
            );
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypt
    @Override
    public byte[] convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;

        try {
            // 1. Wrap object class name and JSON representation
            String targetClassName = attribute.getClass().getName();
            String objectJson = objectMapper.writeValueAsString(attribute);
            String wrapperJson = objectMapper.writeValueAsString(new EncryptedPayload(targetClassName, objectJson));

            // 2. Generate random IV
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            // 3. Encrypt payload
            byte[] cipherText = cipher.doFinal(wrapperJson.getBytes(CHARSET));

            // 4. Pack IV + CipherText into single bytea
            ByteBuffer byteBuffer = ByteBuffer.allocate(IV_LENGTH_BYTE + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return byteBuffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting object field", e);
        }
    }

    // Decrypt
    @Override
    public Object convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) return null;

        try {
            // 1. Extract IV + CipherText
            ByteBuffer byteBuffer = ByteBuffer.wrap(dbData);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // 2. AES-GCM Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            String wrapperJson = new String(cipher.doFinal(cipherText), CHARSET);

            // 3. Extract metadata wrapper and reconstruct original class type
            EncryptedPayload payload = objectMapper.readValue(wrapperJson, EncryptedPayload.class);
            Class<?> targetClass = Class.forName(payload.className());

            return objectMapper.readValue(payload.json(), targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting object field", e);
        }
    }

    // Internal wrapper preserving type metadata
    private record EncryptedPayload(String className, String json) {}
}