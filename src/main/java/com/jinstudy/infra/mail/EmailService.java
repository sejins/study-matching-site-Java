package com.jinstudy.infra.mail;

public interface EmailService { // 이메일 전송 기능을 추상화 할 인터페이스
    void sendEmail(EmailMessage emailMessage);
}
