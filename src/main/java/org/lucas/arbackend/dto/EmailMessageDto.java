package org.lucas.arbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.service.messaging.CustomEmailType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessageDto {
    private String toEmail;
    private String fullName;
    private CustomEmailType customEmailType;
    // Make the otp null to trigger a welcome message instead
    private String otp;
}
