package com.linagora.consistency.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FolderIdTest {

    @Test
    void shouldCreateValidFolderId() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        FolderId folderId = FolderId.of(uuid);
        assertEquals(uuid, folderId.getValue());
    }

    @Test
    void shouldThrowExceptionForNullFolderId() {
        assertThrows(IllegalArgumentException.class, () -> FolderId.of(null));
    }

    @Test
    void shouldThrowExceptionForInvalidUUID() {
        assertThrows(IllegalArgumentException.class, () -> FolderId.of("not-a-uuid"));
        assertThrows(IllegalArgumentException.class, () -> FolderId.of("12345"));
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        FolderId id1 = FolderId.of(uuid);
        FolderId id2 = FolderId.of(uuid);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
