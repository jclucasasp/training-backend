package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRegistrationRequest;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.student.StudentEnrollmentRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepo;
    private final StudentEnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;
    private final OrganisationRepository orgRepo;

    public EnrollmentResponse enrollStudent(Long orgId, Long courseId, StudentRegistrationRequest req) {
        // 1. Find or Create Student (Upsert)
        Student student = studentRepo.findByOrganisationIdAndStStudentNumber(orgId, req.getStudentNumber())
                .orElseGet(() -> {
                    Organisation org = orgRepo.findById(orgId).orElseThrow();
                    Student newStudent = new Student();
                    newStudent.setOrganisation(org);
                    newStudent.setStudentNumber(req.getStudentNumber());
                    newStudent.setName(req.getFirstName());
                    newStudent.setLastName(req.getLastName());
                    return studentRepo.save(newStudent);
                });

        // 2. Check if already enrolled
        if (enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new IllegalStateException("Student already enrolled");
        }

        // 3. Create Enrollment
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        StudentEnrollment saved = enrollmentRepo.save(enrollment);

        return EnrollmentResponse.builder()
                .enrollmentId(saved.getId())
                .courseName(course.getName())
                .enrolledAt(saved.getEnrolledAt())
                .progressPercentage(0.0)
                .build();
    }
}
