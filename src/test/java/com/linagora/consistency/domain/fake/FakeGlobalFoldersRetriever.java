package com.linagora.consistency.domain.fake;

import com.linagora.consistency.domain.model.GlobalFolder;
import com.linagora.consistency.domain.port.driven.ForRetrievingGlobalFolders;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of ForRetrievingGlobalFolders for testing.
 * Test Double pattern - Fake (not a Mock).
 */
public class FakeGlobalFoldersRetriever implements ForRetrievingGlobalFolders {

    private final List<GlobalFolder> globalFolders = new ArrayList<>();

    public void addGlobalFolder(GlobalFolder folder) {
        globalFolders.add(folder);
    }

    @Override
    public List<GlobalFolder> retrieveAllGlobalFolders() {
        return new ArrayList<>(globalFolders);
    }

    public void clear() {
        globalFolders.clear();
    }
}
