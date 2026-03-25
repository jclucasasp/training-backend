package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
public class OrganisationRequest {
    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Email(message = "Must be a valid email address")
    private String email;

    // Organisation
    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 50, message = "Organisation fileName must be between 3 and 50 characters long")
    private String orgName;

    // Profile
    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Pattern(
            regexp = "^\\d{4}/\\d{1,6}/\\d{2}$",
    message = "Registration number must follow the format YYYY/NNNNNN/SS"
    )
    private String registrationNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Pattern(regexp = "^4\\d{9}$",
            message = "VAT number must be a valid 10-digit number starting with 4")
    private String vatNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 20, message = "Contact person firstname must be between 3 and 20 characters long")
    private String firstName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 20, message = "Contact person firstname must be between 3 and 20 characters long")
    private String lastName;

    // TODO: When going international, add the below dependency and remove the regex
//    <dependency>
//    <groupId>com.googlecode.libphonenumber</groupId>
//    <artifactId>libphonenumber</artifactId>
//    <version>8.13.x</version>
//   </dependency>

    @Pattern(
    regexp = "^(\\+27|0)[6-8][0-9]{8}$",
    message = "Invalid South African mobile number. Use 07x/08x... or +277x/278x...")
    private String contactNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 50, message = "Street fileName must be between 3 and 50 characters long")
    private String street;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 50, message = "Suburb fileName must be between 3 and 50 characters long")
    private String suburb;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 50, message = "City/Town fileName must be between 3 and 20 characters long")
    private String city;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 20, message = "State/Region fileName must be between 3 and 20 characters long")
    private String state;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Pattern(regexp = "^[0-9]{4}$", message = "Zip code must be 4 characters long")
    private String zip;

}

