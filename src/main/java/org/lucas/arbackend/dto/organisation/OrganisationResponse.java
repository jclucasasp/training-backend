package org.lucas.arbackend.dto.organisation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.AccessLevelViews;

import java.time.LocalDateTime;

@Data @Builder
public class OrganisationResponse {

    @JsonView(AccessLevelViews.Public.class)
    private Long id;
    @JsonView(AccessLevelViews.Public.class)
    private String orgName;
    @JsonView(AccessLevelViews.Public.class)
    private String registrationNumber;
    @JsonView(AccessLevelViews.Public.class)
    private String vatNumber;

    @JsonView(AccessLevelViews.Public.class)
    private String firstName;
    @JsonView(AccessLevelViews.Public.class)
    private String lastName;
    @JsonView(AccessLevelViews.Public.class)
    private String contactNumber;
    @JsonView(AccessLevelViews.Public.class)
    private String email;

    // Address
    @JsonView(AccessLevelViews.Public.class)
    private String streetAddress;
    @JsonView(AccessLevelViews.Public.class)
    private String suburb;
    @JsonView(AccessLevelViews.Public.class)
    private String city;
    @JsonView(AccessLevelViews.Public.class)
    private String state;
    @JsonView(AccessLevelViews.Public.class)
    private String zip;

    @JsonView(AccessLevelViews.Public.class)
    private String apiKey;
    @JsonView(AccessLevelViews.Public.class)
    private String subscriptionPlan;
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime subscriptionStartDate;
    @JsonView(AccessLevelViews.Public.class)
    private Boolean subscriptionStatus;
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime subscriptionEndDate;

    // Meta Data
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime createdAt;
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime updatedAt;
    @JsonView(AccessLevelViews.Internal.class)
    private LocalDateTime endedAt;
}
