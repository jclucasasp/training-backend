package org.lucas.arbackend.controller;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.security.AccessRequest;
import org.lucas.arbackend.dto.student.helper.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.service.student.StudentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class StudentController {

    private final StudentService studentService;

    // Dashboard Endpoint: Get all students
    @GetMapping("/org/students")
    public List<StudentResponse> getOrgStudents(@AuthenticationPrincipal Organisation org) {
        return studentService.getAllByOrg(org.getId());
    }

    // AR App Endpoint: Public access point
    @PostMapping("/public/access")
    public StudentResponse accessPlatform(@RequestBody AccessRequest request) {
                return studentService.getOrCreateStudentAccess(request.getApiKey(), request.getStudentNumber());
    }
}
