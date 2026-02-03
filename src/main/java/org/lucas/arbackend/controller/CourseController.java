package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseCreateRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.service.course.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "2. Courses", description = "Curriculum creation and retrieval with pagination")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "List Org Courses (Paginated)",
               description = "Returns a list of active courses. Use 'page' and 'size' parameters for optimization.")
    @GetMapping("/org/{orgId}")
    public ResponseEntity<Page<CourseResponse>> getCourses(
            @PathVariable Long orgId,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(courseService.getPaginatedCourses(orgId, pageable));
    }

    @Operation(summary = "Create Full Course Tree",
               description = "Creates a course with nested modules and sections in a single request.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course and modules created"),
        @ApiResponse(responseCode = "403", description = "Course limit reached for this subscription plan")
    })
    @PostMapping("/{orgId}")
    public ResponseEntity<CourseResponse> createCourse(@PathVariable Long orgId, @RequestBody CourseCreateRequest request) {
        return ResponseEntity.ok(courseService.createCourse(orgId, request));
    }
}
