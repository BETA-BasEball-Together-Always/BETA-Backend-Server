package com.beta.infra.repository;

import com.beta.domain.entity.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogJpaRepository extends JpaRepository<AdminLog, Long> {
}
