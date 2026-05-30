package com.myrrhax.alertservice.service;

import com.myrrhax.alertservice.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {
    private final EmailService emailService;

    // ToDo add localization
    private final String MESSAGE_PATTERN = """
            Alert: %s
            Threshold: %.2f
            Energy Consumed: %.2f
            """;

    @KafkaListener(topics = "energy-alerts")
    public void consumeAlertEvent(@Payload AlertingEvent event) {
        log.info("Received alerting event: {}", event);

        // ToDo Add localization
        String subject = "Energy Usage Alert for User: " + event.userId();
        String message = String.format(MESSAGE_PATTERN,
                event.message(),
                event.threshold(),
                event.energyConsumed());

        try {
            emailService.sendEmail(event.email(),
                    subject,
                    message,
                    event.userId());
        } catch (Exception e) {
            log.error("Failed to process event", e);

            throw e;
        }
    }
}
