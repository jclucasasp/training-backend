package org.lucas.arbackend.dto.organisation;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.util.AccessLevelViews;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(name = "StaffResponse", description = "The structural endpoint response mapping fully parsed, tenant-isolated staff user records")
public class StaffResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "Unique auto-generated internal database sequencing primary index identifier link", example = "42")
    private Long id;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The given name of the staff member", example = "Jane")
    private String firstName;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The family name or surname of the staff member", example = "Doe")
    private String lastName;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The registered operational authentication and communication email address destination", example = "jane.doe@acmeinstitute.com")
    private String email;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The recorded telephone contact communication text line string context link", example = "+27721234567")
    private String contactNumber;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "The explicit system access permission clearance authorization group tier", example = "COURSE_EDITOR")
    private RoleTypes role;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "ISO date-time token detailing when this staff membership account initialization completed", example = "2026-02-10T11:24:00")
    private LocalDateTime createdAt;

    @JsonView(AccessLevelViews.Public.class)
    @Schema(description = "ISO timestamp checkpoint mapping modifications tracking metadata properties", example = "2026-05-18T09:12:33")
    private LocalDateTime updatedAt;

    @JsonView(AccessLevelViews.Internal.class)
    @Schema(description = "ISO boundary record pinning explicit account deprecation, resignation, or termination points events data", example = "2027-02-10T00:00:00")
    private LocalDateTime endedAt;
}