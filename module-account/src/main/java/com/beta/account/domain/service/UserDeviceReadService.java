package com.beta.account.domain.service;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.infra.repository.UserDeviceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDeviceReadService {

    private final UserDeviceJpaRepository userDeviceJpaRepository;

    public Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId) {
        return userDeviceJpaRepository.findByUserIdAndDeviceId(userId, deviceId);
    }
}
