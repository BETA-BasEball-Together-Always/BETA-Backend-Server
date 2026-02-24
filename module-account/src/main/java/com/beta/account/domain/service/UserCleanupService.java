package com.beta.account.domain.service;

import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserConsentJpaRepository;
import com.beta.account.infra.repository.UserDeviceJpaRepository;
import com.beta.account.infra.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserJpaRepository userJpaRepository;
    private final UserConsentJpaRepository userConsentJpaRepository;
    private final UserDeviceJpaRepository userDeviceJpaRepository;
    private final CommunityDataCleanupPort communityDataCleanupPort;

    private static final int WITHDRAWAL_RETENTION_DAYS = 30;

    @Transactional
    public int cleanupExpiredWithdrawnUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(WITHDRAWAL_RETENTION_DAYS);
        List<User> expiredUsers = userJpaRepository.findExpiredWithdrawnUsers(threshold);

        if (expiredUsers.isEmpty()) {
            log.info("No expired withdrawn users to cleanup");
            return 0;
        }

        log.info("Found {} expired withdrawn users to cleanup", expiredUsers.size());

        for (User user : expiredUsers) {
            try {
                deleteUserData(user.getId());
                log.info("Successfully cleaned up user data for userId={}", user.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup user data for userId={}", user.getId(), e);
            }
        }

        return expiredUsers.size();
    }

    private void deleteUserData(Long userId) {
        communityDataCleanupPort.deleteAllUserCommunityData(userId);
        userDeviceJpaRepository.deleteAllByUserId(userId);
        userConsentJpaRepository.deleteByUserId(userId);
        userJpaRepository.deleteById(userId);
    }
}
