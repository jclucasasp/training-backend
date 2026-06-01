package org.lucas.arbackend.dto.organisation;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.lucas.arbackend.util.AccessLevelViews;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(name = "OrganisationResponse", description = "The structural endpoint payload containing fully hydrated metadata for an organisation entity")
public class OrganisationResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Unique database auto-incrementing primary sequence index structural identification link value", example = "104")
    private Long id;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The registered corporate trading or institution brand title profile name", example = "Acme Training Group")
    private String orgName;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The official business setup registration verification identifier layout index structure code", example = "2026/123456/07")
    private String registrationNumber;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Tax categorization assessment parameter token lookup record", example = "4123456789")
    private String vatNumber;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "First name of the administrative contact associated with this profile", example = "Lucas")
    private String firstName;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Last name of the administrative contact associated with this profile", example = "Smith")
    private String lastName;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Verified mobile phone communication mapping connection text link", example = "+27821234567")
    private String contactNumber;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Primary administrative system profile communication address location account routing context link", example = "admin@acmecorp.com")
    private String email;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Street name and number coordinates matching delivery details", example = "123 Innovation Way")
    private String streetAddress;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Subdivision geographical neighborhood identifier element map tracking target", example = "Century City")
    private String suburb;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The specific metropolitan area town index mapping category", example = "Cape Town")
    private String city;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The operational province boundary node location designation structural segment", example = "Western Cape")
    private String state;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The matching regional 4-digit postal layout code identification string", example = "7441")
    private String zip;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The unique x-api-key alphanumeric token tracking isolated multitenant transaction layer routing protocols", example = "org_sk_live_a1b2c3d4e5f6g7h8i9j0k")
    private String apiKey;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The pricing plan model selection assigned to this account tier hierarchy profile status code", example = "ENTERPRISE")
    private String subscriptionPlan;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The licensing recurring cost billing amount calculated under contract constraints metrics", example = "2499.00")
    private Double subscriptionAmount;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "ISO date-time tracking token detailing activation cycle processing start events parameters records", example = "2026-01-15T08:30:00")
    private LocalDateTime subscriptionStartDate;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Operational evaluation tracking lifecycle status markers mapping engine components", example = "ACTIVE")
    private String subscriptionStatus;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Tracking record pinpointing administrative updates applied to subscription properties details maps", example = "2026-05-10T14:15:22")
    private String subscriptionUpdatedDate;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "ISO timeline parameters checkpoint outlining the termination cycle boundary conditions specifications", example = "2027-01-15T23:59:59")
    private LocalDateTime subscriptionEndDate;

}
