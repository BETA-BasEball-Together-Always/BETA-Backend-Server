package com.beta.domain.service;

import com.beta.domain.entity.AdminLog;
import com.beta.infra.repository.AdminLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLogWriteService {

    private final AdminLogJpaRepository adminLogJpaRepository;

    public AdminLog save(AdminLog adminLog) {
        return adminLogJpaRepository.save(adminLog);
    }
}
