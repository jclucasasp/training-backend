package org.lucas.arbackend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.config.RabbitConfig;
import org.lucas.arbackend.dto.EmailMessageDto;
import org.lucas.arbackend.service.messaging.CustomEmailType;
import org.lucas.arbackend.service.messaging.EmailProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {
    private final Random random = new SecureRandom();
    private final Map<String, String> otpHashMap = new HashMap<>();
    private final EmailProducer emailProducer;

    public void otpTimer(String email, String fullName) {
        if (otpHashMap.get(email) != null) {
            log.info("OTP already exist, doing nothing...");
            return;
        }

        String otp = generateOtp(6);

        log.info("OTP Timer started for user: [{}]", email);
        otpHashMap.putIfAbsent(email,otp);

        EmailMessageDto emailJob = EmailMessageDto.builder()
                .toEmail(email)
                .fullName(fullName)
                .otp(otp)
                .build();

        emailProducer.queueEmail(fullName, email, otp, CustomEmailType.RESET);

        Timer timer = new Timer(email);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("OTP expired for user [{}]", email);
                otpHashMap.remove(email);
            }
        };
        // expires 10 minutes
        timer.schedule(task, 600000);
    }

    public String getOtpHashMap(String email) {
        return otpHashMap.get(email);
    }

    public String generateOtp(int length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }
}
