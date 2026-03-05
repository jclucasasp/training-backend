package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.lucas.arbackend.mapper.StudentMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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
    private final QuizRepository quizRepo;
    private final StudentQuizRepository studentQuizRepo;

    // ==========================================
    // 1. ENROLLMENT LOGIC (UPSERT Student)
    // ==========================================
    public EnrollmentResponse enrollStudent(StudentRequest request) {

        // Verify Organisation
        Organisation org = findOrganisation();

        MappingContext ctx = new MappingContext(org, null, null);

        // Find or Create student within this Org
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(org.getId(), request.getStudentNumber())
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    studentMapper.updateStudent(request, newStudent, ctx);

                    return studentRepo.save(newStudent);
                });

        // Check if course exists
        Course course = courseRepo.findByOrganisationIdAndSlugAndEndedAtIsNull(org.getId(), request.getSlug())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Create Enrollment
        StudentEnrollment enrollment = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseGet(() -> {
                            StudentEnrollment newEnrollment = new StudentEnrollment();
                            newEnrollment.setStudent(student);
                            newEnrollment.setCourse(course);
                            newEnrollment.setOrganisation(org);
                            return enrollmentRepo.save(newEnrollment);
                        }
                );

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentNumber(student.getStudentNumber())
                .courseName(course.getName())
                .enrolledAt(enrollment.getEnrolledAt())
                .totalProgress(BigDecimal.ZERO)
                .build();
    }

    // ==========================================
    // 2. PROGRESS TRACKING
    // ==========================================
    @Transactional
    public void updateProgress(String studentNumber, Long sectionId, Double percentage) {
        // 1. Context Resolution (Org & Student)
        Organisation org = findOrganisation();

        Student student = findStudent(studentNumber, org);

        // 2. Resolve the Section first (needed for both Enrollment lookup and Progress)
        ChapterSection currentSection = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // 3. Find Enrollment (Scoped by Student and the Course this section belongs to)
        StudentEnrollment enrollment = findEnrollment(student, currentSection);

        // 4. Update the "Pointer" for Resume functionality
        enrollment.setChapterSection(currentSection);

        // 5. Track the specific Section Progress
        handleSectionProgress(enrollment, currentSection, percentage, org);

        BigDecimal total = calculateTotalProgress(enrollment);
        enrollment.setTotalProgress(total);

        // 6. Optional: Check if the whole Course is now 100% complete
        if (total.compareTo(BigDecimal.valueOf(100)) > 0 && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        enrollmentRepo.save(enrollment);
    }

    private void handleSectionProgress(StudentEnrollment enrollment, ChapterSection section, Double percentage, Organisation org) {
        // Look for existing progress for this specific enrollment + section
        StudentProgress progress = enrollment.getStudentProgresses().stream()
                .filter(p -> p.getChapterSection().getId().equals(section.getId()))
                .findFirst()
                .orElseGet(() -> {
                    StudentProgress newStudentProgress = StudentProgress.builder()
                            .studentEnrollment(enrollment)
                            .chapterSection(section)
                            .organisation(org)
                            .percentage(0.0)
                            .isCompleted(false)
                            .build();

                    enrollment.getStudentProgresses().add(newStudentProgress);
                    return newStudentProgress;
                });

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

    private BigDecimal calculateTotalProgress(StudentEnrollment enrollment) {
        Course course = enrollment.getCourse();

        int completedMinutes = enrollment.getStudentProgresses().stream()
                .filter(StudentProgress::getIsCompleted)
                .mapToInt(p -> p.getChapterSection().getDurationInMinutes() != null
                        ? p.getChapterSection().getDurationInMinutes() : 0)
                .sum();

        if (course.getTotalTimeInMinutes() == null || course.getTotalTimeInMinutes() == 0) {
            return BigDecimal.ZERO;
        }

        double rate = (double) completedMinutes / course.getTotalTimeInMinutes() * 100;
        return BigDecimal.valueOf(Math.min(rate, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getPaginatedStudents(Pageable pageable) {
        return studentRepo.findAllByOrganisationId(tenantProvider.get(), pageable)
                .map(studentMapper::maptToStudentResponse);
    }

    public void registerStudentForQuiz(String studentNumber, Long quizId) {
        Organisation org = findOrganisation();

        Student student = findStudent(studentNumber, org);

        Quiz quiz = findQuiz(quizId, org.getId());

        boolean isEnrolled = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), quiz.getCourse().getId()).isPresent();
        if (!isEnrolled) {
            throw new IllegalStateException("Student must be enrolled in a course to be assigned to a quiz");
        }

        if (studentQuizRepo.existsByStudentAndQuiz(student, quiz)) {
            throw new IllegalStateException("Student is already registered for this quiz");
        }

        StudentQuiz assignment = StudentQuiz.builder()
                .student(student)
                .quiz(quiz)
                .organisation(org)
                .build();

        studentQuizRepo.save(assignment);
    }

     // ==========================================
    // 3. RESUME & QUIZ SUBMISSION
    // ==========================================

    @Transactional(readOnly = true)
    public EnrollmentResponse getResumeDetails(String studentNumber, String courseSlug) {
     StudentEnrollment enrollment = findEnrollment(studentNumber, courseSlug);

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .courseName(enrollment.getCourse().getName())
                .totalProgress(enrollment.getTotalProgress())
                // Provide the ID of the section they last viewed
                .lastSectionId(enrollment.getChapterSection() != null ? enrollment.getChapterSection().getId() : null)
                .build();
    }

    @Transactional
    public void submitQuiz(String studentNumber, Long quizId, BigDecimal score) {
        StudentQuiz studentQuiz = findStudentQuiz(studentNumber, quizId);

        studentQuiz.setScore(score);
        studentQuiz.setCompletedAt(LocalDateTime.now());

        // If score is above a certain threshold, you could mark it as passed
        studentQuiz.setPassed(score.compareTo(BigDecimal.valueOf(70)) >= 0);

        studentQuizRepo.save(studentQuiz);
    }

 @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentDashboard(String studentNumber) {
        Long orgId = tenantProvider.get();
        return enrollmentRepo.findAllByStudentOrganisationIdAndStudentStudentNumber(orgId, studentNumber)
                .stream()
                .map(e -> EnrollmentResponse.builder()
                        .enrollmentId(e.getId())
                        .courseName(e.getCourse().getName())
                        .totalProgress(e.getTotalProgress()) // This saves us from having to map through the studentProgresses set on every view
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();
    }

    private Organisation findOrganisation() {
        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
    }

    private StudentEnrollment findEnrollment(String studentNumber, String courseSlug){
        return enrollmentRepo.findByStudentOrganisationIdAndStudentStudentNumberAndCourseSlug(
                        tenantProvider.get(), studentNumber, courseSlug)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
    }

    private StudentEnrollment findEnrollment(Student student, ChapterSection currentSection){
        return enrollmentRepo.findByStudentAndChapterSection(student, currentSection)
                .orElseThrow(() -> new EntityNotFoundException("No active enrollment found for this course section"));
    }

    private StudentQuiz findStudentQuiz(String studentNumber, Long quizId){
        return studentQuizRepo.findByStudentOrganisationIdAndStudentStudentNumberAndQuizId(
                        tenantProvider.get(), studentNumber, quizId)
                .orElseThrow(() -> new EntityNotFoundException("Student is not registered for this quiz"));
    }

    private Student findStudent(String studentNumber, Organisation org) {
        return studentRepo.findByOrganisationAndStudentNumber(org, studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }

    private Quiz findQuiz(Long quizId, Long orgId) {
        return quizRepo.findByIdAndOrganisationIdAndEndedAtIsNull(quizId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found or belongs to another organisation"));
    }

}
