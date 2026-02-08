package com.linagora.consistency.domain.fake;

import com.linagora.consistency.domain.model.Email;
import com.linagora.consistency.domain.model.UserFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUserFolders;

import java.util.HashMap;
import java.util.Map;

/**
 * Fake implementation of ForRetrievingUserFolders for testing.
 * Test Double pattern - Fake (not a Mock).
 */
public class FakeUserFoldersRetriever implements ForRetrievingUserFolders {

    private final Map<Email, UserFolders> userFoldersMap = new HashMap<>();

    public void addUserFolders(UserFolders userFolders) {
        userFoldersMap.put(userFolders.getUserEmail(), userFolders);
    }

    @Override
    public UserFolders retrieveFoldersForUser(Email userEmail) {
        return userFoldersMap.getOrDefault(userEmail, UserFolders.of(userEmail, java.util.Collections.emptyList()));
    }

    public void clear() {
        userFoldersMap.clear();
    }
}
