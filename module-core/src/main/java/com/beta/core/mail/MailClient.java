package com.beta.core.mail;

public interface MailClient {
    void send(String to, String subject, String content);
}
