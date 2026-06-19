package org.lucas.arbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessageDto {
    private String toEmail;
    private String fullName;
    // Make the otp null to trigger a welcome message instead
    private String otp;
}
