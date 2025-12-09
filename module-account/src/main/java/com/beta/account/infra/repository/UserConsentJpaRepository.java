package com.beta.account.infra.repository;

import com.beta.account.domain.entity.UserConsents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConsentJpaRepository extends JpaRepository<UserConsents, Long> {

}
