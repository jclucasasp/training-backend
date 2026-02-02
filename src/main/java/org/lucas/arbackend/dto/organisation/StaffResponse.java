package org.lucas.arbackend.dto.organisation;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data @Builder
public class StaffResponse {
    private Long id;
    private String email;
    private String role;
    private boolean isActive;
}
