package com.beta.account.application.port;

public interface CommunityDataCleanupPort {

    void deleteUserBlockRelationships(Long userId);
}
