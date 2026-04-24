package com.beta.account.domain.service;

import com.beta.account.domain.entity.EmailOutbox;
import com.beta.account.domain.entity.EmailOutbox.EmailOutboxStatus;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.EmailOutboxJpaRepository;
import com.beta.core.mail.MailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeEmailService {

    private static final int MAX_RETRY_COUNT = 5;

    private final MailClient mailClient;
    private final EmailOutboxJpaRepository emailOutboxJpaRepository;
    private final UserReadService userReadService;

    @Async("mailExecutor")
    public void sendWelcomeEmail(Long userId, String email, String nickName) {
        log.info("회원가입 환영 메일 발송 시작: userId={}, email={}, nickName={}", userId, email, nickName);

        try {
            deliverWelcomeEmail(email, nickName);
        } catch (RuntimeException e) {
            saveOutboxFailure(userId, e);
        }
    }

    @Transactional
    public int retryFailedWelcomeEmails() {
        LocalDateTime now = LocalDateTime.now();
        List<EmailOutbox> outboxes = emailOutboxJpaRepository
                .findTop20ByStatusInAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
                        List.of(EmailOutboxStatus.PENDING, EmailOutboxStatus.RETRYING),
                        now
                );

        if (outboxes.isEmpty()) {
            return 0;
        }

        for (EmailOutbox outbox : outboxes) {
            try {
                User user = userReadService.findUserById(outbox.getUserId());
                deliverWelcomeEmail(user.getEmail(), user.getNickname());
                outbox.markSent();
            } catch (RuntimeException e) {
                handleRetryFailure(outbox, e);
            }
        }

        return outboxes.size();
    }

    public void deliverWelcomeEmail(String email, String nickName) {
        String subject = "[BETA] 회원가입이 완료되었습니다!";
        String content = buildWelcomeEmailContent(nickName);

        mailClient.send(email, subject, content);
    }

    public void saveOutboxFailure(Long userId, RuntimeException e) {
        try {
            emailOutboxJpaRepository.save(EmailOutbox.createWelcomeMailFailure(
                    userId,
                    truncateErrorMessage(e.getMessage()),
                    LocalDateTime.now().plusMinutes(1)
            ));
        } catch (RuntimeException saveException) {
            log.error("이메일 아웃박스 저장 실패: userId={}, error={}", userId, saveException.getMessage(), saveException);
        }
    }

    public void handleRetryFailure(EmailOutbox outbox, RuntimeException e) {
        String errorMessage = truncateErrorMessage(e.getMessage());
        int nextRetryCount = outbox.getRetryCount() + 1;

        if (nextRetryCount >= MAX_RETRY_COUNT) {
            outbox.markDead(errorMessage);
            return;
        }

        outbox.markRetrying(errorMessage, calculateNextRetryAt(nextRetryCount));
    }

    public LocalDateTime calculateNextRetryAt(int retryCount) {
        return switch (retryCount) {
            case 1 -> LocalDateTime.now().plusMinutes(5);
            case 2 -> LocalDateTime.now().plusMinutes(30);
            case 3 -> LocalDateTime.now().plusHours(2);
            default -> LocalDateTime.now().plusHours(6);
        };
    }

    public String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "메일 발송 실패";
        }
        if (errorMessage.length() <= 1000) {
            return errorMessage;
        }
        return errorMessage.substring(0, 1000);
    }

    private String buildWelcomeEmailContent(String nickName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h1 style="color: #1a1a1a; text-align: center; margin-bottom: 30px;">[BETA] 회원가입이 완료되었습니다!</h1>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8;">
                            안녕하세요 <strong>%s</strong>님! 야구를 더 뜨겁게 즐기는 공간 BETA입니다. ⚾🔥
                        </p>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8;">
                            회원님의 BETA 회원가입이 정상적으로 완료되었습니다.
                        </p>
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 25px 0; border-radius: 4px;">
                            <p style="color: #856404; font-size: 14px; line-height: 1.6; margin: 0;">
                                만약 해당 이메일 주소로 회원가입 또는 로그인을 시도한 적이 없다면,<br>
                                보안상 반드시 아래 이메일로 즉시 연락 부탁드립니다.
                            </p>
                        </div>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8; text-align: center;">
                            📩 <a href="mailto:betaofficial365@gmail.com" style="color: #007bff; text-decoration: none;">betaofficial365@gmail.com</a>
                        </p>
                        <p style="color: #666666; font-size: 14px; line-height: 1.6; text-align: center; margin-top: 20px;">
                            담당자가 빠른 시일 내에 확인하여 조치 해드리겠습니다.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
                        <p style="color: #999999; font-size: 12px; text-align: center;">
                            BETA - BasEball Together Always
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(nickName);
    }
}
