package org.lucas.arbackend.dto.organisation;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data @Builder
public class OrganisationResponse {
    private Long id;
    private String email;
    private String orgName;
    private String subscriptionStatus;
}
