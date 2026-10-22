package me.diegomcha.autoparte.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;


public class ValidationsTest {

    @Test
    void validatesNifAndNieChecksums() {
        Assertions.assertTrue(Validations.isValidNif("00000000T"));
        Assertions.assertTrue(Validations.isValidNif("X1234567L"));
        Assertions.assertFalse(Validations.isValidNif("00000000A"));
        Assertions.assertFalse(Validations.isValidNif("1234567Z"));
        Assertions.assertFalse(Validations.isValidNif("12345678a"));
    }

    @Test
    void ensuresValidNif() {
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidNif("00000000T"));

        var exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Validations.ensureValidNif("00000000A")
        );
        Assertions.assertEquals("Invalid NIF format", exception.getMessage());
    }

    @Test
    void ensuresValidEmail() {
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidEmail("person@example.com"));
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidEmail("person+tag@example.co.uk"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Validations.ensureValidEmail("invalid"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Validations.ensureValidEmail("person@example"));
    }

    @Test
    void validatesAndEnsuresPhoneNumbers() {
        Assertions.assertTrue(Validations.isValidPhone("971 49 28 05"));
        Assertions.assertTrue(Validations.isValidPhone("+1 (314) 849-1998"));
        Assertions.assertFalse(Validations.isValidPhone("invalid"));

        Assertions.assertDoesNotThrow(() -> Validations.ensureValidPhone("+34 971492805"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Validations.ensureValidPhone("invalid"));
    }

    @Test
    void ensuresValidSpanishMunicipalityCodes() {
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidSpanishMunicipalityCode("07040"));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Validations.ensureValidSpanishMunicipalityCode("7040")
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Validations.ensureValidSpanishMunicipalityCode("07A40")
        );
    }

    @Test
    void ensuresValidSpanishPostalCodesMatchMunicipality() {
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidSpanishPostalCode("07040", "07040"));
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidSpanishPostalCode("07001", "07040"));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Validations.ensureValidSpanishPostalCode("7040", "07040")
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Validations.ensureValidSpanishPostalCode("08040", "07040")
        );
    }

    @Test
    void ensuresValidCountry() {
        Assertions.assertDoesNotThrow(() -> Validations.ensureValidCountry("ESP"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Validations.ensureValidCountry("Spain"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Validations.ensureValidCountry("ZZZ"));
    }

    @Test
    void detectsImageContent() {
        var png = new MockMultipartFile(
                "image",
                "document.png",
                "image/png",
                new byte[]{
                        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
                }
        );
        var text = new MockMultipartFile(
                "image",
                "document.txt",
                "text/plain",
                "not an image".getBytes()
        );

        Assertions.assertTrue(Validations.isValidImage(png));
        Assertions.assertFalse(Validations.isValidImage(text));
    }

    @Test
    void wrapsImageReadFailures() {
        var unreadableFile = new MockMultipartFile("image", "document.png", "image/png", new byte[0]) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("cannot read image");
            }
        };

        var exception = Assertions.assertThrows(
                UncheckedIOException.class,
                () -> Validations.isValidImage(unreadableFile)
        );
        Assertions.assertEquals("cannot read image", exception.getCause().getMessage());
    }
}
