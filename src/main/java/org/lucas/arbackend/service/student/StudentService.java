package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.ProgressUpdateRequest;
import org.lucas.arbackend.dto.student.StudentEnrollRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.Section;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.SectionRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.student.StudentEnrollmentRepository;
import org.lucas.arbackend.repository.student.StudentProgressRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepo;
    private final StudentEnrollmentRepository enrollmentRepo;
    private final StudentProgressRepository progressRepo;
    private final OrganisationRepository orgRepo;
    private final CourseRepository courseRepo;
    private final SectionRepository sectionRepo;
    private final ApiKeyRepository apiRepo;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. ENROLLMENT LOGIC (UPSERT Student)
    // ==========================================
    public EnrollmentResponse enrollStudent(Long orgId, StudentEnrollRequest request) {

        // Verify Organisation
        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        // Find or Create student within this Org
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(orgId, request.getStudentNumber())
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    newStudent.setOrganisation(org);
                    newStudent.setStudentNumber(request.getStudentNumber());
                    newStudent.setFirstName(request.getFirstName());
                    newStudent.setLastName(request.getLastName());
                    return studentRepo.save(newStudent);
                });

        // Check if course exists
        Course course = courseRepo.findByOrganisationIdAndEndedAtIsNull(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Create Enrollment
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        StudentEnrollment saved = enrollmentRepo.save(enrollment);

        return EnrollmentResponse.builder()
                .enrollmentId(saved.getId())
                .studentNumber(student.getStudentNumber())
                .courseName(course.getName())
                .enrolledAt(saved.getEnrolledAt())
                .currentTotalProgress(BigDecimal.ZERO)
                .build();
    }

    // ==========================================
    // 2. PROGRESS TRACKING
    // ==========================================
    public void updateSectionProgress(ProgressUpdateRequest request) {
        StudentEnrollment enrollment = enrollmentRepo.findById(request.getEnrollmentId())
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        Section section = sectionRepo.findById(request.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // Check if progress entry already exists for this section, otherwise create new
        StudentProgress progress = progressRepo.findByEnrollmentIdAndSectionId(request.getEnrollmentId(), request.getSectionId())
                .orElseGet(() -> {
                    StudentProgress newProgress = new StudentProgress();
                    newProgress.setEnrollment(enrollment);
                    newProgress.setSection(section);
                    return newProgress;
                });

        // TODO: Implement check on section completion and calculate the progress percentage
//        progress.setPercentage(request.getPercentage());
//        progressRepo.save(progress);
//
//        // If progress is 100%, check if course is completed
//        if (request.getPercentage().compareTo(new BigDecimal("100.00")) >= 0) {
//            checkAndMarkCompletion(enrollment);
//        }
    }

    private void checkAndMarkCompletion(StudentEnrollment enrollment) {
        // Logic to compare total sections in course vs sections completed in progress table
        // Update enrollment.setCompletedAt(LocalDateTime.now()) if finished
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getPaginatedStudents(Long orgId, Pageable pageable) {
        return studentRepo.findAllByOrganisationId(orgId, pageable)
                .map(s -> StudentResponse.builder()
                        .id(s.getId())
                        .studentNumber(s.getStudentNumber())
                        .firstName(s.getFirstName())
                        .lastName(s.getLastName())
                        .build());
    }
}
