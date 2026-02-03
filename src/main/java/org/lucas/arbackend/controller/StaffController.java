package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.organisation.CreateStaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.service.staff.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Tag(name = "4. Staff Management", description = "Management of Proxy users and internal permissions")
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "Add Staff Member",
               description = "Creates a staff account with a specific role from the RoleTypes enum.")
    @PostMapping("/{orgId}")
    public ResponseEntity<StaffResponse> addStaff(@PathVariable Long orgId, @Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(staffService.createStaff(orgId, request));
    }
}
