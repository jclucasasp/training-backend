package org.lucas.arbackend.dto.organisation;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data @Builder
public class OrganisationResponse {
    private Long id;
    private String email;
    private String orgName;
    private String registrationNumber;
    private String vatNumber;
    private String apiKey;
    private LocalDateTime orgSignedUpDate;
    private LocalDateTime orgLastUpdated;
    private LocalDateTime orgDeletedDate;
    private String subscriptionPlan;
    private LocalDateTime subscriptionStartDate;
    private Boolean subscriptionStatus;
    private LocalDateTime subscriptionEndDate;
}
