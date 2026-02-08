package com.linagora.consistency.domain.fake;

import com.linagora.consistency.domain.model.Email;
import com.linagora.consistency.domain.port.driven.ForRetrievingUsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of ForRetrievingUsers for testing.
 * Test Double pattern - Fake (not a Mock).
 */
public class FakeUserRetriever implements ForRetrievingUsers {

    private final List<Email> users = new ArrayList<>();

    public void addUser(Email email) {
        users.add(email);
    }

    @Override
    public List<Email> retrieveAllUsers() {
        return new ArrayList<>(users);
    }

    public void clear() {
        users.clear();
    }
}
