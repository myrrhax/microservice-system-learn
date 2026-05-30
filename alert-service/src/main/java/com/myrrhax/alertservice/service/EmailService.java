package com.myrrhax.alertservice.service;

import com.myrrhax.alertservice.entity.Alert;
import com.myrrhax.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;

    public void sendEmail(String to, String subject, String body, Long userId) {
        log.info("Sending email to: {}, subject: {}", to, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@myrr.com");
        message.setSubject(subject);
        message.setText(body);

        boolean sent = false;
        try {
            mailSender.send(message);
            sent = true;

            log.info("Email sent to: {}, subject: {}", to, subject);
        } catch (MailException e) {
            log.error("Failed to send email to: {}", to, e);
        }

        Alert alertSent = buildAlertEntity(userId, sent);
        alertRepository.save(alertSent);

        log.info("Alert info for {} was saved", to);
    }

    private static @NonNull Alert buildAlertEntity(Long userId, boolean sent) {
        return new Alert(userId,
                OffsetDateTime.now(ZoneId.of("Europe/Moscow")),
                sent);
    }
}
