package com.beta.account.infra.repository;

import com.beta.account.domain.entity.EmailOutbox;
import com.beta.account.domain.entity.EmailOutbox.EmailOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EmailOutboxJpaRepository extends JpaRepository<EmailOutbox, Long> {

    List<EmailOutbox> findTop20ByStatusInAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            Collection<EmailOutboxStatus> statuses,
            LocalDateTime nextRetryAt
    );
}
