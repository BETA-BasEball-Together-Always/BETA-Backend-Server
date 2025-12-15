package com.beta.account.domain.service;

import com.beta.core.mail.MailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordEmailService {

    private final MailClient mailClient;

    @Async("mailExecutor")
    public void sendPasswordCord(String email, String nickName, String code) {
        log.info("비밀번호 인증코드 메일 발송 시작: email={}, nickName={}", email, nickName);

        String subject = "[BETA] 비밀번호 찾기 인증코드 입니다.!";
        String content = buildPasswordEmailContent(nickName, code);

        mailClient.send(email, subject, content);
    }

    private String buildPasswordEmailContent(String nickName, String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h1 style="color: #1a1a1a; text-align: center; margin-bottom: 30px;">[BETA] 비밀번호 찾기 인증코드</h1>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8;">
                            안녕하세요 <strong>%s</strong>님! 야구를 더 뜨겁게 즐기는 공간 BETA입니다. ⚾🔥
                        </p>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8;">
                            비밀번호 찾기 요청에 대한 인증코드를 안내드립니다.
                        </p>
                        <div style="background-color: #f8f9fa; border: 2px solid #007bff; padding: 30px; margin: 25px 0; border-radius: 8px; text-align: center;">
                            <p style="color: #666666; font-size: 14px; margin: 0 0 10px 0;">인증코드</p>
                            <h2 style="color: #007bff; font-size: 36px; letter-spacing: 8px; margin: 0; font-family: 'Courier New', monospace;">%s</h2>
                        </div>
                        <p style="color: #333333; font-size: 16px; line-height: 1.8; text-align: center;">
                            위 인증코드를 비밀번호 찾기 화면에 입력해주세요.
                        </p>
                        <p style="color: #dc3545; font-size: 14px; line-height: 1.6; text-align: center; margin-top: 20px;">
                            ⏰ 인증코드는 <strong>2분</strong> 동안만 유효합니다.
                        </p>
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 25px 0; border-radius: 4px;">
                            <p style="color: #856404; font-size: 14px; line-height: 1.6; margin: 0;">
                                ⚠️ 만약 비밀번호 찾기를 요청하지 않으셨다면,<br>
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
                """.formatted(nickName, code);
    }
}
