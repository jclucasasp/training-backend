package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.course.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "4. Courses", description = "Curriculum creation and retrieval with pagination")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "List Org Courses (Paginated)",
            description = "Returns a list of active courses. Use 'page', 'size' and 'sort' parameters for optimization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    // 1. Tells Swagger this response uses the PagedModel layout
                                    implementation = PagedModel.class,
                                    // 2. Explicitly forces your CourseResponse definition to be compiled into the components array
                                    anyOf = { CourseResponse.class }
                            )
                    )),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation or Courses not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @GetMapping()
    public ResponseEntity<Page<CourseResponse>> getCourses(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sort) {
        return ResponseEntity.ok(courseService.getPaginatedCourses(PageRequest.of(page, size, Sort.by(sort))));
    }
    // TODO: Create an authorise endpoint to authorise uploads once API's from Google and YouTube has been configured
    @Operation(summary = "Add Course", description = "Creates a new curriculum course for the organization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR')")
    @PostMapping("/course/add")
    public ResponseEntity<CourseResponse> addCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @Operation(summary = "Update Course", description = "Update the course and its corresponding chapters and sections.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN') or (hasAuthority('COURSE_EDITOR') and @courseService.isOwner(#courseId, principal.id))")
    @PutMapping("/{courseId}/update")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @Operation(summary = "Delete Course", description = "Delete the course and its corresponding chapters and sections.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
        })
    @PreAuthorize("hasAuthority('ORG_ADMIN') or (hasAuthority('COURSE_EDITOR') and @courseService.isOwner(#courseId, principal.id))")
    @DeleteMapping("/{courseId}/delete")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.softDeleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

}