package org.lucas.arbackend.dto.helper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrganisationResponse {
    private Long id;
    private  String email;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endedAt;
    private LocalDateTime passwordResetDate;
}
