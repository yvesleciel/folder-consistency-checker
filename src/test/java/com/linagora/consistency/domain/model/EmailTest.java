package com.linagora.consistency.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = Email.of("test@example.com");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldThrowExceptionForNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(null));
    }

    @Test
    void shouldThrowExceptionForBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(""));
        assertThrows(IllegalArgumentException.class, () -> Email.of("   "));
    }

    @Test
    void shouldThrowExceptionForInvalidEmailFormat() {
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid-email"));
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("test@example.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");

        assertNotEquals(email1, email2);
    }
}
