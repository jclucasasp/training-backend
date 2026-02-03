package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentEnrollRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.service.student.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
@Tag(name = "3. Student Management", description = "Student onboarding and enrollment tracking")
public class StudentController {

    private final StudentService studentService;

     @Operation(summary = "Enroll Student in Course",
               description = "Verifies if the student exists in the Org; if not, creates them and starts enrollment.")
    @PostMapping("/org/{orgId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable String apiKey, @RequestBody StudentEnrollRequest request) {
        return ResponseEntity.ok(studentService.enrollStudent(apiKey, request));
    }

    @Operation(summary = "Get Student List", description = "Paginated list of all students registered under this tenant.")
    @GetMapping("/org/{orgId}")
    public ResponseEntity<Page<StudentResponse>> listStudents(@PathVariable Long orgId, Pageable pageable) {
        return ResponseEntity.ok(studentService.getPaginatedStudents(orgId, pageable));
    }

}
