package com.jinstudy.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class  HtmlEmailService implements EmailService{

     private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessage emailMessage) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,false,"UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), true );
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("failed to send email", e);
            throw new RuntimeException(e); // 이메일을 전송하다가 문제가 발생하면 에러 던지게끔 이건 애플리케이션 서버측에서 이메일을 전송하다가 발생한 에러의 경우를 의미하는듯.
            //당연히 네트워크 트래픽에 의해서 이메일이 전송이 안되는 경우는 이것에 해당 안됨.

        }
    }
}
