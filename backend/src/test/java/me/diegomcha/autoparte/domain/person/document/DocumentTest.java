package me.diegomcha.autoparte.domain.person.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// TODO: Split into multiple test classes for each document type
class DocumentTest {

    private Document nifDoc;
    private Document passportDoc;

    @BeforeEach
    void setUp() {
        this.nifDoc = Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT");
        this.passportDoc = Document.of(Document.DocumentType.PASSPORT, "123456789");
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(Document.class, this.passportDoc);
        Assertions.assertInstanceOf(DniDocument.class, this.nifDoc);
    }

    @Test
    void testRequiresSecondSurname() {
        var nieDoc = Document.of(Document.DocumentType.NIE, "Z3268977S", "SUPPORT");

        Assertions.assertTrue(this.nifDoc.requiresSecondSurname());
        Assertions.assertFalse(nieDoc.requiresSecondSurname());
        Assertions.assertFalse(this.passportDoc.requiresSecondSurname());
    }

    @Test
    void testNifValidation() {
        // Invalid NIF format
        Assertions.assertThrows(IllegalArgumentException.class, () -> Document.of(Document.DocumentType.NIF, "INVALID", "SUPPORT"));
        // Invalid NIF letter
        Assertions.assertThrows(IllegalArgumentException.class, () -> Document.of(Document.DocumentType.NIE, "Z3268977A", "SUPPORT"));
        // Valid NIF
        Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT");
        // Valid NIE
        Document.of(Document.DocumentType.NIE, "Z3268977S", "SUPPORT");
    }
}
