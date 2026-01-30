package org.lucas.arbackend.dto.helper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class SignUpResponse {

    private Long orgId;
    private String orgName;
    private String email;
    private String registrationNumber;
    private String vatNumber;
    private LocalDateTime createAt;
    private LocalDateTime endedAt;
    private LocalDateTime updatedAt;
}
