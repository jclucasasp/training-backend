package org.lucas.arbackend.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "OrganisationRequest", description = "Payload layout required to onboard and register a new multi-tenant organisation profile along with its initial root administrator user account")
public class OrganisationRequest {
    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    @Schema(description = "Primary account and contact email address for the organization", example = "admin@acmecorp.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @Schema(description = "Secure access credentials password for the administrative tenant login account", example = "P@ssw0rd2026", minLength = 8, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Organisation name is required")
    @Size(min = 3, max = 50, message = "Organisation name must be between 3 and 50 characters long")
    @Schema(description = "The registered operational trading entity or institutional brand name", example = "Acme Training Group", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Registration number is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{1,6}/\\d{2}$",
            message = "Registration number must follow the format YYYY/NNNNNN/SS"
    )
    @Schema(description = "Official statutory corporate registration context code string formatting", example = "2026/123456/07", pattern = "^\\d{4}/\\d{1,6}/\\d{2}$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String registrationNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "VAT number is required")
    @Pattern(regexp = "^4\\d{9}$",
            message = "VAT number must be a valid 10-digit number starting with 4")
    @Schema(description = "South African statutory value-added tax account parsing index matching verification rules", example = "4123456789", pattern = "^4\\d{9}$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vatNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Contact first name is required")
    @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters long")
    @Schema(description = "Given name of the organization's primary administrative representative", example = "Lucas", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Contact last name is required")
    @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters long")
    @Schema(description = "Surname of the organization's primary administrative representative", example = "Smith", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Invalid mobile number format. Use 07x/08x... or +277x/278x...")
    @Schema(description = "Direct telephone context link utilizing South African mobile matching validations", example = "0821234567", pattern = "^(\\+27|0)[6-8][0-9]{8}$")
    private String contactNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Street address is required")
    @Size(min = 3, max = 50, message = "Street name must be between 3 and 50 characters long")
    @Schema(description = "Building location number accompanied by local street directory naming labels", example = "123 Innovation Way", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String street;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Suburb details are required")
    @Size(min = 3, max = 50, message = "Suburb name must be between 3 and 50 characters long")
    @Schema(description = "Local municipal sector area or residential suburb subdivision node", example = "Century City", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String suburb;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "City details are required")
    @Size(min = 3, max = 50, message = "City/Town name must be between 3 and 20 characters long")
    @Schema(description = "The broader metropolitan city domain node location boundary context mapping", example = "Cape Town", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "State/Region details are required")
    @Size(min = 3, max = 20, message = "State/Region name must be between 3 and 20 characters long")
    @Schema(description = "The territorial state, province, or geopolitical district area region descriptor", example = "Western Cape", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String state;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Postal/Zip code is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Zip code must be 4 characters long")
    @Schema(description = "The specific 4-digit localized postal sorting code allocation lookup mapping status context", example = "7441", pattern = "^[0-9]{4}$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String zip;

    // TODO: When going international, add the below dependency and remove the regex
//    <dependency>
//    <groupId>com.googlecode.libphonenumber</groupId>
//    <artifactId>libphonenumber</artifactId>
//    <version>8.13.x</version>
//   </dependency>
}

