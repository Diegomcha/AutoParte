package me.diegomcha.autoparte.core.security;

import me.diegomcha.autoparte.config.AutoparteProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;


class PersistenceEncryptionConverterTest {

    private PersistenceEncryptionConverter converter;

    @BeforeEach
    void setUp() {
        var properties = new AutoparteProperties();
        properties.getSecurity().setDbEncryptionKey("test-key-1234567");
        converter = new PersistenceEncryptionConverter(properties, JsonMapper.builder().build());
    }

    @Test
    void convertsNullValuesToNull() {
        Assertions.assertNull(converter.convertToDatabaseColumn(null));
        Assertions.assertNull(converter.convertToEntityAttribute(null));
        Assertions.assertNull(converter.convertToEntityAttribute(new byte[0]));
    }

    @Test
    void encryptsAndDecryptsTypedObjects() {
        var original = new TestPayload("alice", 42, true);

        var encrypted = converter.convertToDatabaseColumn(original);
        var decrypted = converter.convertToEntityAttribute(encrypted);

        Assertions.assertEquals(original, decrypted);
        Assertions.assertEquals(TestPayload.class, decrypted.getClass());
    }

    @Test
    void encryptsStringsAndPreservesTheirType() {
        var encrypted = converter.convertToDatabaseColumn("sensitive value");

        Assertions.assertEquals("sensitive value", converter.convertToEntityAttribute(encrypted));
        Assertions.assertEquals(String.class, converter.convertToEntityAttribute(encrypted).getClass());
    }

    @Test
    void generatesDifferentCiphertextForEachEncryption() {
        var first = converter.convertToDatabaseColumn(new TestPayload("alice", 42, true));
        var second = converter.convertToDatabaseColumn(new TestPayload("alice", 42, true));

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
        Assertions.assertFalse(Arrays.equals(first, second));
        Assertions.assertTrue(first.length > 12);
        Assertions.assertTrue(second.length > 12);
    }

    @Test
    void rejectsInvalidEncryptionKeyLength() {
        var properties = new AutoparteProperties();
        properties.getSecurity().setDbEncryptionKey("too-short");
        var jsonMapper = JsonMapper.builder().build();

        var exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new PersistenceEncryptionConverter(properties, jsonMapper)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid AES key length"));
    }

    @Test
    void rejectsTamperedCiphertext() {
        var encrypted = converter.convertToDatabaseColumn(new TestPayload("alice", 42, true));

        Assertions.assertNotNull(encrypted);
        encrypted[encrypted.length - 1] ^= 1;

        var exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> converter.convertToEntityAttribute(encrypted)
        );

        Assertions.assertEquals("Error decrypting object field", exception.getMessage());
    }

    public record TestPayload(String name, int age, boolean active) {
    }
}
