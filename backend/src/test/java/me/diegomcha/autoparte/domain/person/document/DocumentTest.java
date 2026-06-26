package me.diegomcha.autoparte.domain.person.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentTest {

    private Document passportDoc;
    private Document otherDoc;

    @BeforeEach
    void setUp() {
        this.passportDoc = Document.of(Document.DocumentType.PASSPORT, "123456789");
        this.otherDoc = Document.of(Document.DocumentType.OTHER, "987654321");
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertSame(Document.class, this.passportDoc.getClass());
        Assertions.assertSame(Document.class, this.otherDoc.getClass());
    }

    @Test
    void testRequiresSecondSurname() {
        Assertions.assertFalse(this.passportDoc.requiresSecondSurname());
        Assertions.assertFalse(this.otherDoc.requiresSecondSurname());
    }
}

