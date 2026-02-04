package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseCreateRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.dto.organisation.CreateStaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.service.course.CourseService;
import org.lucas.arbackend.service.staff.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "4. Admin", description = "Management of staff, courses and internal permissions")
public class AdminController {

    private final StaffService staffService;
    private final CourseService courseService;

    @Operation(summary = "Add Staff Member",
               description = "Creates a staff account with a specific role from the RoleTypes enum.")
    @PostMapping("/staff/{orgId}")
    public ResponseEntity<StaffResponse> addStaff(@PathVariable Long orgId, @Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(staffService.createStaff(orgId, request));
    }

    @Operation(summary = "Create Full Course Tree",
            description = "Creates a course with nested modules and sections in a single request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course and modules created"),
            @ApiResponse(responseCode = "403", description = "Course limit reached for this subscription plan")
    })

    @PostMapping("/course/{orgId}/create")
    public ResponseEntity<CourseResponse> createCourse(@PathVariable Long orgId, @RequestBody CourseCreateRequest request) {
        return ResponseEntity.ok(courseService.createCourse(orgId, request));
    }
}
