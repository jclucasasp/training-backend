package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class OrgDetailsRequest {
    @Email(message = "Must be a valid email address")
    @NotNull(message = "Email address is required")
    private String email;

    // Organisation
    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Organisation name is required")
    private String orgName;

    // Profile
    @NotNull(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "VAT number is required")
    private String vatNumber;

    // OrgAddress
    @NotNull(message = "Contact number is required")
    private Integer contactNumber;

    @NotNull(message = "Contact person is required")
    private String contactPerson;

    @NotNull(message = "Street is required")
    private String street;

    @NotNull(message = "Suburb is required")
    private String suburb;

    @NotNull(message = "City is required")
    private String city;

    @NotNull(message = "State is required")
    private String state;

    @NotNull(message = "Zip is required")
    private Integer zip;

    // OrganisationSubscription
    // TODO: Delete this field once a payment gate has been added. This is solely for testing
    private Long initialPlanId; // Optional, defaults to Monthly Plan

}

