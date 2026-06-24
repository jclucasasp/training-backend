package org.lucas.arbackend.service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.config.RabbitConfig;
import org.lucas.arbackend.dto.EmailMessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailSender emailSender;

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void consumeEmailJob(EmailMessageDto message) {
        log.info("Processing background email job for user: [{}] - Mode: [{}]", message.getToEmail(), message.getOtp() != null ? "OTP" : "Welcome");

        try {
            emailSender.sendEmail(message.getToEmail(), message.getFullName(), message.getOtp(), message.getCustomEmailType());
        } catch (Exception e) {
            log.error("Critical failure during background email dispatch to [{}]", message.getToEmail(), e);
        }
    }
}
