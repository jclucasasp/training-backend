package org.lucas.arbackend.service.messaging;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.config.RabbitConfig;
import org.lucas.arbackend.dto.EmailMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void queueEmail(String fullName, String toEmail, String otp, CustomEmailType emailType) {

        EmailMessageDto messageDto = EmailMessageDto
                .builder()
                .toEmail(toEmail)
                .fullName(fullName)
                .otp(otp)
                .customEmailType(emailType)
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.EMAIL_EXCHANGE, RabbitConfig.EMAIL_ROUTING_KEY, messageDto);
    }
}
