package com.beta.account.infra.repository;

import com.beta.account.domain.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceJpaRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByFcmToken(String fcmToken);

    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId);

    void deleteAllByUserId(Long userId);

    void deleteByUserIdAndDeviceId(Long userId, String deviceId);

    @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
           "AND d.isActive = true")
    List<UserDevice> findActiveDevicesByUserId(@Param("userId") Long userId);
}
