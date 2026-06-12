package me.diegomcha.autoparte.domain.person.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentInfoTest {

    private DocumentInfo nifDoc;
    private DocumentInfo passportDoc;

    @BeforeEach
    void setUp() {
        this.nifDoc = DocumentInfo.of(DocumentInfo.DocumentType.NIF, "54095720L", "SUPPORT");
        this.passportDoc = DocumentInfo.of(DocumentInfo.DocumentType.PASSPORT, "123456789");
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(DocumentInfo.class, this.passportDoc);
        Assertions.assertInstanceOf(DniDocumentInfo.class, this.nifDoc);
    }

    @Test
    void testRequiresSecondSurname() {
        var nieDoc = DocumentInfo.of(DocumentInfo.DocumentType.NIE, "Z3268977S", "SUPPORT");

        Assertions.assertTrue(this.nifDoc.requiresSecondSurname());
        Assertions.assertFalse(nieDoc.requiresSecondSurname());
        Assertions.assertFalse(this.passportDoc.requiresSecondSurname());
    }

    @Test
    void testNifValidation() {
        // Invalid NIF format
        Assertions.assertThrows(IllegalArgumentException.class, () -> DocumentInfo.of(DocumentInfo.DocumentType.NIF, "INVALID", "SUPPORT"));
        // Invalid NIF letter
        Assertions.assertThrows(IllegalArgumentException.class, () -> DocumentInfo.of(DocumentInfo.DocumentType.NIE, "Z3268977A", "SUPPORT"));
        // Valid NIF
        DocumentInfo.of(DocumentInfo.DocumentType.NIF, "54095720L", "SUPPORT");
        // Valid NIE
        DocumentInfo.of(DocumentInfo.DocumentType.NIE, "Z3268977S", "SUPPORT");
    }
}
