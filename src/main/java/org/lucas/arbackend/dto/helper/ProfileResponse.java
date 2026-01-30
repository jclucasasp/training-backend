package org.lucas.arbackend.dto.helper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ProfileResponse {
    private Long orgId;
    private String orgName;
    private String registrationNumber;
    private String vatNumber;
    private LocalDateTime updatedAt;
}
