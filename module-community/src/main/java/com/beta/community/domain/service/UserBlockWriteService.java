package com.beta.community.domain.service;

import com.beta.community.domain.entity.UserBlock;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBlockWriteService {

    private final UserBlockJpaRepository userBlockJpaRepository;

    public UserBlock save(UserBlock userBlock) {
        return userBlockJpaRepository.save(userBlock);
    }

    public void delete(UserBlock userBlock) {
        userBlockJpaRepository.delete(userBlock);
    }
}
