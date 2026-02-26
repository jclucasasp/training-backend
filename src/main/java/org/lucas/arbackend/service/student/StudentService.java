package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.lucas.arbackend.mapper.StudentMapper;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.student.StudentEnrollmentRepository;
import org.lucas.arbackend.repository.student.StudentProgressRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ChapterSectionRepository sectionRepo;
    private final TenantProvider tenantProvider;
    private final StudentMapper studentMapper;

    // ==========================================
    // 1. ENROLLMENT LOGIC (UPSERT Student)
    // ==========================================
    public EnrollmentResponse enrollStudent(StudentRequest request) {

        // Verify Organisation
        Organisation org = orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        // Find or Create student within this Org
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(org.getId(), request.getStudentNumber())
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    studentMapper.updateStudent(request, newStudent);
                    newStudent.setOrganisation(org);

                    return studentRepo.save(newStudent);
                });

        // Check if course exists
        Course course = courseRepo.findByOrganisationIdAndEndedAtIsNull(org.getId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Create Enrollment
        StudentEnrollment enrollment = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseGet(() -> {
                            StudentEnrollment newEnrollment = new StudentEnrollment();
                            newEnrollment.setStudent(student);
                            newEnrollment.setCourse(course);
                            return enrollmentRepo.save(newEnrollment);
                        }
                );

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentNumber(student.getStudentNumber())
                .courseName(course.getName())
                .enrolledAt(enrollment.getEnrolledAt())
                .currentTotalProgress(BigDecimal.ZERO)
                .build();
    }
    // TODO: Update the actual progress from Course.estimatedCompletionTime
    // ==========================================
    // 2. PROGRESS TRACKING
    // ==========================================
    @Transactional
    public void updateProgress(String studentNumber, Long sectionId, Double percentage) {
        // 1. Context Resolution (Org & Student)
        Organisation org = orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Student student = studentRepo.findByOrganisationAndStudentNumber(org, studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // 2. Resolve the Section first (needed for both Enrollment lookup and Progress)
        ChapterSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // 3. Find Enrollment (Scoped by Student and the Course this section belongs to)
        StudentEnrollment enrollment = enrollmentRepo.findByStudentAndChapterSection(student, section)
                .orElseThrow(() -> new EntityNotFoundException("No active enrollment found for this course section"));

        // 4. Update the "Pointer" for Resume functionality
        enrollment.setChapterSection(section);

        // 5. Track the specific Section Progress
        handleSectionProgress(enrollment, section, percentage);

        // 6. Optional: Check if the whole Course is now 100% complete
        checkAndMarkCourseCompletion(enrollment);
    }

    private void handleSectionProgress(StudentEnrollment enrollment, ChapterSection section, Double percentage) {
        // Look for existing progress for this specific enrollment + section
        StudentProgress progress = progressRepo.findByStudentEnrollmentAndChapterSection(enrollment, section)
                .orElseGet(() -> StudentProgress.builder()
                        .studentEnrollment(enrollment)
                        .chapterSection(section)
                        .percentage(0.0)
                        .isCompleted(false)
                        .build());

        // Update percentage only if the new progress is higher (don't let progress go backwards)
        if (percentage > progress.getPercentage()) {
            progress.setPercentage(percentage);
        }

        // Mark as completed if percentage hits 100 (or your specific threshold)
        if (percentage >= 100.0) {
            progress.setIsCompleted(true);
        }

        progressRepo.save(progress);
    }

    private void checkAndMarkCourseCompletion(StudentEnrollment enrollment) {
        long totalSections = sectionRepo.countByChapterCourse(enrollment.getCourse());
        long completedSections = progressRepo.countByStudentEnrollmentAndIsCompletedTrue(enrollment);

        if (totalSections > 0 && totalSections == completedSections) {
            enrollment.setCompletedAt(LocalDateTime.now());
            enrollmentRepo.save(enrollment);
        }
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getPaginatedStudents(Pageable pageable) {
        return studentRepo.findAllByOrganisationId(tenantProvider.get(), pageable)
                .map(studentMapper::maptToStudentResponse);
    }

}
