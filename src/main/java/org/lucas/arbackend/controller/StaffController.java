package org.lucas.arbackend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.lucas.arbackend.dto.organisation.StaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.staff.StaffService;
import org.lucas.arbackend.util.AccessLevelViews;
import org.lucas.arbackend.util.ValidatedLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "3. Staff", description = "Staff management endpoints")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PreAuthorize("hasAuthority('ORG_ADMIN')")
    @PostMapping("/add")
    @Operation(summary = "Add Staff Member", description = "Creates a staff account with a specific role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff member created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Org Admins can add staff",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: Email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<StaffResponse> addStaff(@Validated(ValidatedLabel.OnCreate.class) @RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.createStaff(request));
    }

    @Operation(summary = "Update Staff Member", description = "Updates the details of a staff member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Staff member not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("principal.id == #staffId")
    @PutMapping("/{staffId}/update/details")
    public ResponseEntity<StaffResponse> updateStaff(@PathVariable Long staffId, @Validated(ValidatedLabel.OnUpdate.class) @RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaffDetails(staffId, request));
    }

    @Operation(summary = "List All Staff", description = "Returns a paginated list of all staff members.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN')")
    @GetMapping("")
    public ResponseEntity<Page<StaffResponse>> getAllStaff(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(staffService.getAllStaff(Pageable.ofSize(size).withPage(page)));
    }

    @Operation(summary = "Update Staff Member Access", description = "Updates the access details of a staff member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Staff member not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN')")
    @PutMapping("/{staffId}/update/role")
    public ResponseEntity<StaffResponse> updateStaffRole(@PathVariable Long staffId, @RequestBody RoleTypes role) throws BadRequestException {
        return ResponseEntity.ok(staffService.updateStaffRole(staffId, role));
    }

    @Operation(summary = "Soft Delete Staff Member", description = "Marks a staff member as inactive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Staff deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Staff not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN')")
    @DeleteMapping("/{staffId}/delete")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long staffId) {
        staffService.softDeleteStaff(staffId);
        return ResponseEntity.noContent().build();
    }

}
