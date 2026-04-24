package com.beta.account.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "email_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailOutbox extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mail_type", nullable = false, length = 30)
    private MailType mailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    public EmailOutbox(Long userId, MailType mailType, EmailOutboxStatus status,
                       int retryCount, LocalDateTime nextRetryAt, String lastError,
                       LocalDateTime sentAt) {
        this.userId = userId;
        this.mailType = mailType;
        this.status = status;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.sentAt = sentAt;
    }

    public static EmailOutbox createWelcomeMailFailure(Long userId, String lastError, LocalDateTime nextRetryAt) {
        return EmailOutbox.builder()
                .userId(userId)
                .mailType(MailType.WELCOME)
                .status(EmailOutboxStatus.PENDING)
                .retryCount(0)
                .lastError(lastError)
                .nextRetryAt(nextRetryAt)
                .build();
    }

    public void markSent() {
        this.status = EmailOutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.lastError = null;
        this.nextRetryAt = null;
    }

    public void markRetrying(String lastError, LocalDateTime nextRetryAt) {
        this.status = EmailOutboxStatus.RETRYING;
        this.retryCount += 1;
        this.lastError = lastError;
        this.nextRetryAt = nextRetryAt;
    }

    public void markDead(String lastError) {
        this.status = EmailOutboxStatus.DEAD;
        this.retryCount += 1;
        this.lastError = lastError;
        this.nextRetryAt = null;
    }

    public enum MailType {
        WELCOME
    }

    public enum EmailOutboxStatus {
        PENDING,
        RETRYING,
        SENT,
        DEAD
    }
}
