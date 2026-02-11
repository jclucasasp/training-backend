package org.lucas.arbackend.dto.organisation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class OrganisationResponse {
    private Long id;
    private String orgName;
    private String registrationNumber;
    private String vatNumber;

    private String firstName;
    private String lastName;
    private Integer contactNumber;
    private String email;

    // Address
    private String streetAddress;
    private String suburb;
    private String city;
    private String state;
    private Integer zip;

    private String apiKey;
    private LocalDateTime orgSignedUpDate;
    private LocalDateTime orgLastUpdated;
    private LocalDateTime orgDeletedDate;
    private String subscriptionPlan;
    private LocalDateTime subscriptionStartDate;
    private Boolean subscriptionStatus;
    private LocalDateTime subscriptionEndDate;
}
