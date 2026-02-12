package org.lucas.arbackend.dto.organisation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class StaffResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
