package me.diegomcha.autoparte.domain.person.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DniDocumentTest {

    private Document nifDoc;
    private Document nieDoc;

    @BeforeEach
    void setUp() {
        this.nifDoc = Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT");
        this.nieDoc = Document.of(Document.DocumentType.NIE, "Z3268977S", "SUPPORT");
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(DniDocument.class, this.nifDoc);
        Assertions.assertInstanceOf(DniDocument.class, this.nieDoc);
    }

    @Test
    void testRequiresSecondSurname() {
        Assertions.assertTrue(this.nifDoc.requiresSecondSurname());
        Assertions.assertFalse(this.nieDoc.requiresSecondSurname());
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
