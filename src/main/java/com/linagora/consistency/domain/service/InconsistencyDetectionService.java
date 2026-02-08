package com.linagora.consistency.domain.service;

import com.linagora.consistency.domain.model.*;
import com.linagora.consistency.domain.port.driven.ForRetrievingGlobalFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUserFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUsers;
import com.linagora.consistency.domain.port.driving.ForDetectingInconsistencies;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Domain service implementing the core business logic for inconsistency detection.
 * Pure domain logic without any framework dependencies.
 * Uses ExecutorService for parallel processing to optimize performance.
 */
public class InconsistencyDetectionService implements ForDetectingInconsistencies {

    private final ForRetrievingUsers userRetriever;
    private final ForRetrievingUserFolders userFoldersRetriever;
    private final ForRetrievingGlobalFolders globalFoldersRetriever;
    private final ExecutorService executorService;

    public InconsistencyDetectionService(
        ForRetrievingUsers userRetriever,
        ForRetrievingUserFolders userFoldersRetriever,
        ForRetrievingGlobalFolders globalFoldersRetriever,
        ExecutorService executorService
    ) {
        this.userRetriever = Objects.requireNonNull(userRetriever, "userRetriever cannot be null");
        this.userFoldersRetriever = Objects.requireNonNull(userFoldersRetriever, "userFoldersRetriever cannot be null");
        this.globalFoldersRetriever = Objects.requireNonNull(globalFoldersRetriever, "globalFoldersRetriever cannot be null");
        this.executorService = Objects.requireNonNull(executorService, "executorService cannot be null");
    }

    @Override
    public InconsistencyReport detectInconsistencies() {
        // Fetch global folders (single call)
        List<GlobalFolder> globalFolders = globalFoldersRetriever.retrieveAllGlobalFolders();

        // Fetch all users
        List<Email> users = userRetriever.retrieveAllUsers();

        // Fetch user folders in parallel using ExecutorService
        List<UserFolders> allUserFolders = fetchAllUserFoldersInParallel(users);

        // Detect inconsistencies
        List<Inconsistency> inconsistencies = compareAndDetectInconsistencies(allUserFolders, globalFolders);

        return InconsistencyReport.of(inconsistencies);
    }

    private List<UserFolders> fetchAllUserFoldersInParallel(List<Email> users) {
        List<Callable<UserFolders>> tasks = users.stream()
            .map(email -> (Callable<UserFolders>) () -> userFoldersRetriever.retrieveFoldersForUser(email))
            .toList();

        try {
            List<Future<UserFolders>> futures = executorService.invokeAll(tasks);
            List<UserFolders> results = new ArrayList<>();

            for (Future<UserFolders> future : futures) {
                results.add(future.get());
            }

            return results;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching user folders", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error fetching user folders", e.getCause());
        }
    }

    private List<Inconsistency> compareAndDetectInconsistencies(
        List<UserFolders> allUserFolders,
        List<GlobalFolder> globalFolders
    ) {
        List<Inconsistency> inconsistencies = new ArrayList<>();

        // Index global folders by (user, folderId) for efficient lookup
        Map<Email, Map<FolderId, GlobalFolder>> globalFoldersByUser = indexGlobalFoldersByUser(globalFolders);

        // Check each user's folders against global data
        for (UserFolders userFolders : allUserFolders) {
            Email userEmail = userFolders.getUserEmail();
            Map<FolderId, GlobalFolder> userGlobalFolders = globalFoldersByUser.getOrDefault(userEmail, Collections.emptyMap());

            // Check for inconsistencies in user folders
            Set<FolderId> checkedFolderIds = new HashSet<>();

            for (UserFolder userFolder : userFolders.getFolders()) {
                FolderId folderId = userFolder.getId();
                checkedFolderIds.add(folderId);

                if (!userGlobalFolders.containsKey(folderId)) {
                    // Folder exists in user data but not in global data
                    inconsistencies.add(Inconsistency.missingInGlobal(
                        folderId,
                        userEmail,
                        userFolder.getName()
                    ));
                } else {
                    // Check if names match
                    GlobalFolder globalFolder = userGlobalFolders.get(folderId);
                    if (!userFolder.getName().equals(globalFolder.getName())) {
                        inconsistencies.add(Inconsistency.nameMismatch(
                            folderId,
                            userEmail,
                            globalFolder.getName(),
                            userFolder.getName()
                        ));
                    }
                }
            }

            // Check for folders in global data but not in user data
            for (Map.Entry<FolderId, GlobalFolder> entry : userGlobalFolders.entrySet()) {
                if (!checkedFolderIds.contains(entry.getKey())) {
                    GlobalFolder globalFolder = entry.getValue();
                    inconsistencies.add(Inconsistency.missingInUserFolders(
                        globalFolder.getId(),
                        userEmail,
                        globalFolder.getName()
                    ));
                }
            }
        }

        return inconsistencies;
    }

    private Map<Email, Map<FolderId, GlobalFolder>> indexGlobalFoldersByUser(List<GlobalFolder> globalFolders) {
        return globalFolders.stream()
            .collect(Collectors.groupingBy(
                GlobalFolder::getUserEmail,
                Collectors.toMap(
                    GlobalFolder::getId,
                    folder -> folder,
                    (existing, replacement) -> existing // Keep first in case of duplicates
                )
            ));
    }
}
