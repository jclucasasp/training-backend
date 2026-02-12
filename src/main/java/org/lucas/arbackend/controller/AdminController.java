package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseCreateRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.dto.course.CourseUpdateRequest;
import org.lucas.arbackend.dto.organisation.CreateStaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.service.OrganisationService;
import org.lucas.arbackend.service.course.CourseService;
import org.lucas.arbackend.service.staff.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "4. Admin", description = "Management of staff, courses and internal permissions")
public class AdminController {

    private final StaffService staffService;
    private final CourseService courseService;

    // TODO: Implement deletion for Organisation, Course, Module, Section and Student

    @Operation(summary = "Add Staff Member",
               description = "Creates a staff account with a specific role from the RoleTypes enum.")
    @PostMapping("/staff/create")
    public ResponseEntity<StaffResponse> addStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(staffService.createStaff(request));
    }

    @Operation(summary = "Create Full Course Tree",
            description = "Creates a course with nested modules and sections in a single request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course and modules created"),
            @ApiResponse(responseCode = "403", description = "Course limit reached for this subscription plan"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })

    @PostMapping("/course/create")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @Operation(summary = "Update Course",
            description = "Updates an existing course including its modules and sections.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "403", description = "You do not have permission to update this course"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })

    @PutMapping("/course/update/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @Parameter(description = "ID of the course to update") @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @Operation(summary = "Get All Staff",
    description = "Fetches all staff members for the current organisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "You do not have the correct access for that"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })

    @GetMapping("/staff/all")
    public ResponseEntity<Page<StaffResponse>> getAllStaff(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(staffService.getAllStaff(Pageable.ofSize(size).withPage(page)));
    }

    @Operation(summary = "Update Staff Member",
            description = "Updates the details of a staff member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff updated successfully"),
            @ApiResponse(responseCode = "401", description = "You do not have the correct access for that"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })

    @PutMapping("/staff/update")
    public ResponseEntity<StaffResponse> updateStaff(@RequestParam Long staffId, @Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(staffId, request));
    }

    @DeleteMapping("/staff/delete/{staffId}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long staffId) {

        staffService.softDeleteStaff(staffId);

        return ResponseEntity.ok().build();
    }
}
