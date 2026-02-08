package com.linagora.consistency.domain.service;

import com.linagora.consistency.domain.fake.FakeGlobalFoldersRetriever;
import com.linagora.consistency.domain.fake.FakeUserFoldersRetriever;
import com.linagora.consistency.domain.fake.FakeUserRetriever;
import com.linagora.consistency.domain.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InconsistencyDetectionService using Fake doubles.
 * No mocking framework used - pure Test Doubles.
 */
class InconsistencyDetectionServiceTest {

    private FakeUserRetriever userRetriever;
    private FakeUserFoldersRetriever userFoldersRetriever;
    private FakeGlobalFoldersRetriever globalFoldersRetriever;
    private ExecutorService executorService;
    private InconsistencyDetectionService service;

    @BeforeEach
    void setUp() {
        userRetriever = new FakeUserRetriever();
        userFoldersRetriever = new FakeUserFoldersRetriever();
        globalFoldersRetriever = new FakeGlobalFoldersRetriever();
        executorService = Executors.newFixedThreadPool(2);

        service = new InconsistencyDetectionService(
            userRetriever,
            userFoldersRetriever,
            globalFoldersRetriever,
            executorService
        );
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void shouldReturnEmptyReportWhenNoData() {
        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertNotNull(report);
        assertEquals(0, report.getTotalCount());
        assertTrue(report.getInconsistencies().isEmpty());
        assertFalse(report.hasInconsistencies());
    }

    @Test
    void shouldDetectNoInconsistenciesWhenDataIsConsistent() {
        // Given
        Email userEmail = Email.of("john@example.com");
        FolderId folderId = FolderId.of("550e8400-e29b-41d4-a716-446655440000");
        FolderName folderName = FolderName.of("Inbox");

        userRetriever.addUser(userEmail);

        UserFolder userFolder = UserFolder.of(folderId, folderName);
        userFoldersRetriever.addUserFolders(UserFolders.of(userEmail, List.of(userFolder)));

        GlobalFolder globalFolder = GlobalFolder.of(folderId, userEmail, folderName);
        globalFoldersRetriever.addGlobalFolder(globalFolder);

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(0, report.getTotalCount());
        assertFalse(report.hasInconsistencies());
    }

    @Test
    void shouldDetectNameMismatch() {
        // Given
        Email userEmail = Email.of("alice@example.com");
        FolderId folderId = FolderId.of("550e8400-e29b-41d4-a716-446655440001");
        FolderName userFolderName = FolderName.of("Inbox");
        FolderName globalFolderName = FolderName.of("INBOX_WRONG");

        userRetriever.addUser(userEmail);

        UserFolder userFolder = UserFolder.of(folderId, userFolderName);
        userFoldersRetriever.addUserFolders(UserFolders.of(userEmail, List.of(userFolder)));

        GlobalFolder globalFolder = GlobalFolder.of(folderId, userEmail, globalFolderName);
        globalFoldersRetriever.addGlobalFolder(globalFolder);

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(1, report.getTotalCount());
        assertTrue(report.hasInconsistencies());

        Inconsistency inconsistency = report.getInconsistencies().get(0);
        assertEquals(InconsistencyType.NAME_MISMATCH, inconsistency.getType());
        assertEquals(folderId, inconsistency.getFolderId());
        assertEquals(userEmail, inconsistency.getUserEmail());
        assertEquals(globalFolderName, inconsistency.getGlobalFolderName().orElseThrow());
        assertEquals(userFolderName, inconsistency.getUserFolderName().orElseThrow());
    }

    @Test
    void shouldDetectMissingInGlobal() {
        // Given
        Email userEmail = Email.of("bob@example.com");
        FolderId folderId = FolderId.of("550e8400-e29b-41d4-a716-446655440002");
        FolderName folderName = FolderName.of("Draft");

        userRetriever.addUser(userEmail);

        UserFolder userFolder = UserFolder.of(folderId, folderName);
        userFoldersRetriever.addUserFolders(UserFolders.of(userEmail, List.of(userFolder)));

        // No global folder added

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(1, report.getTotalCount());

        Inconsistency inconsistency = report.getInconsistencies().get(0);
        assertEquals(InconsistencyType.MISSING_IN_GLOBAL, inconsistency.getType());
        assertEquals(folderId, inconsistency.getFolderId());
        assertEquals(userEmail, inconsistency.getUserEmail());
        assertTrue(inconsistency.getGlobalFolderName().isEmpty());
        assertEquals(folderName, inconsistency.getUserFolderName().orElseThrow());
    }

    @Test
    void shouldDetectMissingInUserFolders() {
        // Given
        Email userEmail = Email.of("charlie@example.com");
        FolderId folderId = FolderId.of("550e8400-e29b-41d4-a716-446655440003");
        FolderName folderName = FolderName.of("Sent");

        userRetriever.addUser(userEmail);

        // No user folder added
        userFoldersRetriever.addUserFolders(UserFolders.of(userEmail, List.of()));

        GlobalFolder globalFolder = GlobalFolder.of(folderId, userEmail, folderName);
        globalFoldersRetriever.addGlobalFolder(globalFolder);

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(1, report.getTotalCount());

        Inconsistency inconsistency = report.getInconsistencies().get(0);
        assertEquals(InconsistencyType.MISSING_IN_USER_FOLDERS, inconsistency.getType());
        assertEquals(folderId, inconsistency.getFolderId());
        assertEquals(userEmail, inconsistency.getUserEmail());
        assertEquals(folderName, inconsistency.getGlobalFolderName().orElseThrow());
        assertTrue(inconsistency.getUserFolderName().isEmpty());
    }

    @Test
    void shouldDetectMultipleInconsistenciesForMultipleUsers() {
        // Given
        Email user1 = Email.of("user1@example.com");
        Email user2 = Email.of("user2@example.com");

        FolderId folder1Id = FolderId.of("550e8400-e29b-41d4-a716-446655440010");
        FolderId folder2Id = FolderId.of("550e8400-e29b-41d4-a716-446655440011");

        userRetriever.addUser(user1);
        userRetriever.addUser(user2);

        // User 1: Name mismatch
        UserFolder user1Folder = UserFolder.of(folder1Id, FolderName.of("Inbox"));
        userFoldersRetriever.addUserFolders(UserFolders.of(user1, List.of(user1Folder)));
        globalFoldersRetriever.addGlobalFolder(GlobalFolder.of(folder1Id, user1, FolderName.of("INBOX_DIFF")));

        // User 2: Missing in global
        UserFolder user2Folder = UserFolder.of(folder2Id, FolderName.of("Archive"));
        userFoldersRetriever.addUserFolders(UserFolders.of(user2, List.of(user2Folder)));

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(2, report.getTotalCount());
        assertEquals(1L, report.getCountsByType().get(InconsistencyType.NAME_MISMATCH));
        assertEquals(1L, report.getCountsByType().get(InconsistencyType.MISSING_IN_GLOBAL));
    }

    @Test
    void shouldHandleMultipleFoldersPerUser() {
        // Given
        Email userEmail = Email.of("multi@example.com");
        FolderId folder1 = FolderId.of("550e8400-e29b-41d4-a716-446655440020");
        FolderId folder2 = FolderId.of("550e8400-e29b-41d4-a716-446655440021");
        FolderId folder3 = FolderId.of("550e8400-e29b-41d4-a716-446655440022");

        userRetriever.addUser(userEmail);

        // User folders
        List<UserFolder> userFolders = List.of(
            UserFolder.of(folder1, FolderName.of("Inbox")),
            UserFolder.of(folder2, FolderName.of("Sent")),
            UserFolder.of(folder3, FolderName.of("Draft"))
        );
        userFoldersRetriever.addUserFolders(UserFolders.of(userEmail, userFolders));

        // Global folders - folder1 matches, folder2 name mismatch, folder3 missing
        globalFoldersRetriever.addGlobalFolder(GlobalFolder.of(folder1, userEmail, FolderName.of("Inbox")));
        globalFoldersRetriever.addGlobalFolder(GlobalFolder.of(folder2, userEmail, FolderName.of("SENT_WRONG")));

        // When
        InconsistencyReport report = service.detectInconsistencies();

        // Then
        assertEquals(2, report.getTotalCount());
        assertTrue(report.hasInconsistencies());
    }
}
