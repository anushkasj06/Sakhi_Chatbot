package com.wms.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wms.config.MailProperties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void sendVerificationEmail(String to, String token) {
        String link = mailProperties.getVerifyBaseUrl() + "?token=" + token;
        send(to, "Verify your WMS account", "Click to verify your email: " + link + "\nToken: " + token);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = mailProperties.getResetBaseUrl() + "?token=" + token;
        send(to, "Reset your WMS password", "Click to reset your password: " + link + "\nToken: " + token);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Email send failed for {}", to, ex);
        }
    }
}
