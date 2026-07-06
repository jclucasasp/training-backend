package org.lucas.arbackend.service.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.quiz.QuizAttemptResponse;
import org.lucas.arbackend.dto.security.StudentTokenResponse;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.quiz.StudentQuizAttempt;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.lucas.arbackend.mapper.StudentMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.quiz.StudentQuizAttemptRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.repository.student.StudentEnrollmentRepository;
import org.lucas.arbackend.repository.student.StudentProgressRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.service.messaging.CustomEmailType;
import org.lucas.arbackend.service.messaging.EmailProducer;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepo;
    private final StudentEnrollmentRepository enrollmentRepo;
    private final StudentProgressRepository progressRepo;
    private final CourseRepository courseRepo;
    private final ChapterSectionRepository sectionRepo;
    private final TenantProvider tenantProvider;
    private final StudentMapper studentMapper;
    private final QuizRepository quizRepo;
    private final StudentQuizRepository studentQuizRepo;
    private final StudentQuizAttemptRepository attemptRepo;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;
    private final CacheService cacheService;
    private final EmailProducer emailProducer;

    public StudentResponse createStudent(String studentNumber, StudentRequest request) {

        if (studentRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }
        // Verify Organisation
        Organisation org = findOrganisation();

        MappingContext ctx = new MappingContext(org, null, null);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);
        // Create Student
        Student student = Student.builder()
                .studentNumber(studentNumber)
                .role(roleRepo.findByRoleName(RoleTypes.STUDENT))
                .organisation(org)
                .build();
        studentMapper.updateStudent(request, student, ctx);

        String fullName = String.join(" ", student.getFirstName(), student.getLastName());
        emailProducer.queueEmail(fullName, student.getEmail(), null, CustomEmailType.WELCOME);

        return studentMapper.mapToStudentResponse(studentRepo.save(student));
    }

    // ==========================================
    // 1. ENROLLMENT LOGIC (UPSERT Student)
    // ==========================================

    public StudentTokenResponse enrollStudent(String studentNumber, String slug) {

        // Verify Organisation
        Organisation org = findOrganisation();

        // Find or Create student within this Org
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(org.getId(), studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // Check if course exists
        Course course = courseRepo.findByOrganisationIdAndSlug(org.getId(), slug)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Create Enrollment
        enrollmentRepo.findByStudentIdAndCourseId(org.getId(), student.getId(), course.getId())
                .orElseGet(() -> {
                            StudentEnrollment newEnrollment = StudentEnrollment.builder()
                                    .student(student)
                                    .course(course)
                                    .organisation(org)
                                    .totalProgress(BigDecimal.ZERO)
                                    .build();
                            return enrollmentRepo.save(newEnrollment);
                        }
                );

        return createStudentToken(studentNumber, student, org.getSubscription().getStatus().equals(1));

    }
    // TODO: Send the student token to Cloudflare R2 bucket. Also need to create a delete request for when the token expires
    private StudentTokenResponse createStudentToken(String studentNumber, Student student, boolean subscriptionStatus) {

        if (!subscriptionStatus) {
            throw new IllegalStateException("Subscription not active");
        }

        var cache = cacheService.getActiveStudentToken(studentNumber);
        if (cache != null) {
            return cache;
        }

        String sessionToken = UUID.randomUUID().toString().replaceAll("-","");
        StudentTokenResponse response = StudentTokenResponse.builder()
                .orgId(student.getOrganisation().getId())
                .studentNumber(studentNumber)
                .studentToken(sessionToken)
                .studentName(student.getFirstName())
                .studentLastname(student.getLastName())
                .createdAt(LocalDateTime.now())
                .isSubscriptionActive(subscriptionStatus)
                .build();

        cacheService.updateCache("student_token", studentNumber, response);

        return response;
    }

    public void removeStudent(String studentNumber) {
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(tenantProvider.get(), studentNumber)
                        .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        cacheService.evictAuthUser(student.getEmail());
        cacheService.evictActiveStudentToken(studentNumber);
        studentRepo.delete(student);
    }

    // ==========================================
    // 2. PROGRESS TRACKING
    // ==========================================
    @Transactional
    public void updateProgress(String studentNumber, Long courseId, Long chapterId, Long sectionId) {
        // 1. Context Resolution (Org & Student)
        Organisation org = findOrganisation();

        Student student = findStudent(org.getId(), studentNumber);

        // 2. Resolve the Section first (needed for both Enrollment lookup and Progress)
        ChapterSection chapterSection = sectionRepo.findWithContext(courseId, chapterId, org.getId(), sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Course course = chapterSection.getChapter().getCourse();

        // 3. Find Enrollment (Scoped by Student and the Course this section belongs to)
        StudentEnrollment enrollment = findEnrollment(student.getId(), course.getId());

        // 5. Track the specific Section Progress
        handleSectionProgress(enrollment, chapterSection, org);

        BigDecimal total = calculateTotalProgress(enrollment, course);

        enrollment.setTotalProgress(total);
        enrollmentRepo.save(enrollment);
    }

    private void handleSectionProgress(StudentEnrollment enrollment, ChapterSection chapterSection, Organisation org) {

    // 1. A section's own tracking row always aims for 100% completion
    BigDecimal sectionPercentage = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);

    // 2. Look for existing progress for this specific enrollment + section
    StudentProgress progress = enrollment.getStudentProgresses().stream()
            .filter(p -> p.getChapterSection().getId().equals(chapterSection.getId()))
            .findFirst()
            .orElseGet(() -> {
                StudentProgress newStudentProgress = StudentProgress.builder()
                        .studentEnrollment(enrollment)
                        .organisation(org)
                        .chapter(chapterSection.getChapter())
                        .chapterSection(chapterSection)
                        .isCompleted(false) // Let the downstream logic handle this cleanly
                        .percentage(BigDecimal.ZERO)
                        .lastAccessedAt(LocalDateTime.now())
                        .build();

                enrollment.getStudentProgresses().add(newStudentProgress);
                return newStudentProgress;
            });

    // 3. Update the section record percentage safely (don't move backward)
    if (sectionPercentage.compareTo(progress.getPercentage()) > 0) {
        progress.setPercentage(sectionPercentage);
    }

    // 4. Mark section as complete when it hits 100
    if (sectionPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
        progress.setIsCompleted(true);
    }
    progress.setLastAccessedAt(LocalDateTime.now());

    // 5. Save the section progress record first to update the database state
    progressRepo.saveAndFlush(progress);

    // 6. NOW it is safe to calculate aggregated snapshots!
    // Since the database/collection is updated, you can safely compute overall metrics:
    BigDecimal finalCourseProgress = calculateTotalProgress(enrollment, enrollment.getCourse());
    enrollment.setTotalProgress(finalCourseProgress);

    if (finalCourseProgress.compareTo(BigDecimal.valueOf(100)) >= 0) {
        enrollment.setCompletedAt(LocalDateTime.now());
    }
    enrollmentRepo.saveAndFlush(enrollment);
}

    private BigDecimal calculateTotalProgress(StudentEnrollment enrollment, Course course) {
        int courseTotalTime = course.getTotalTimeInMinutes();

        int totalCompletedMinutesAcrossCourse = enrollment.getStudentProgresses().stream()
                .filter(StudentProgress::getIsCompleted) // Rely on the child records
                .map(StudentProgress::getChapterSection)
                .filter(Objects::nonNull)
                .mapToInt(ChapterSection::getDurationInMinutes)
                .sum(); // Total time of everything they have finished so far

// 3. Pass the accumulated sum to your utility method safely
        return calculatePercentage(totalCompletedMinutesAcrossCourse, courseTotalTime);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getPaginatedStudents(Pageable pageable) {
        return studentRepo.findAllByOrganisationId(tenantProvider.get(), pageable)
                .map(studentMapper::mapToStudentResponse);
    }

    private BigDecimal calculatePercentage(int sectionCompletedMinutes, int totalMinutes) {
        if (totalMinutes == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal completed = BigDecimal.valueOf(sectionCompletedMinutes);
        BigDecimal total = BigDecimal.valueOf(totalMinutes);

        return completed.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    // TODO: Check if all the sections have been completed on the frontend and give a warning if not
    // Need to loop through all the sections and check if they have been completed
    public void registerStudentForQuiz(String studentNumber, Long quizId) {
        Organisation org = findOrganisation();

        Student student = findStudent(org.getId(), studentNumber);

        Quiz quiz = findQuiz(quizId, org.getId());

        boolean isEnrolled = enrollmentRepo.findByStudentIdAndCourseId(org.getId(), student.getId(), quiz.getCourse().getId()).isPresent();
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
    public StudentTokenResponse getResumeDetails(String studentNumber, String courseSlug) {
        StudentEnrollment enrollment = findEnrollment(studentNumber, courseSlug);
        StudentTokenResponse tokenResponse =  cacheService.getActiveStudentToken(studentNumber);
        if (tokenResponse == null) {
            tokenResponse = createStudentToken(studentNumber, enrollment.getStudent(), enrollment.getOrganisation().getSubscription().getStatus().equals(1));
        }

        return tokenResponse;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentDashboard(String studentNumber) {
        Long orgId = tenantProvider.get();
        return enrollmentRepo.findAllByStudentNumber(orgId, studentNumber)
                .stream()
                .map(e -> EnrollmentResponse.builder()
                        .enrollmentId(e.getId())
                        .studentNumber(studentNumber)
                        .courseName(e.getCourse().getName())
                        .courseSlug(e.getCourse().getSlug())
                        .currentTotalProgress(e.getTotalProgress()) // This saves us from having to map through the studentProgresses set on every view
                        .enrolledAt(e.getEnrolledAt())
                        .completedAt(e.getCompletedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getAttemptDetails(String StudentNumber, Long attemptId) {
        Long currentOrgId = tenantProvider.get();

        StudentQuizAttempt attempt = attemptRepo.findById(attemptId)
                .filter(a -> a.getOrganisation().getId().equals(currentOrgId)
                        && a.getStudent().getStudentNumber().equals(StudentNumber))
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found or access denied"));

        Object parsedAnswers;
        try {
            // Parse the JSON string back into a Map or List for the frontend
            parsedAnswers = objectMapper.readValue(attempt.getSubmittedAnswersJson(), Object.class);
        } catch (JsonProcessingException e) {
            // Fallback if JSON is malformed
            parsedAnswers = attempt.getSubmittedAnswersJson();
        }

        return QuizAttemptResponse.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .score(attempt.getScore())
                .isPassed(attempt.isPassed())
                .completedAt(attempt.getCompletedAt())
                .answers(parsedAnswers)
                .build();
    }

    private Organisation findOrganisation() {
        return tenantProvider.getOrg();
    }

    private StudentEnrollment findEnrollment(String studentNumber, String courseSlug){
        return enrollmentRepo.findByCourseSlug(
                        tenantProvider.get(), studentNumber, courseSlug)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
    }

    private StudentEnrollment findEnrollment(Long studentId, Long courseId){
        return enrollmentRepo.findByStudentIdAndCourseId(tenantProvider.get(), studentId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("No active enrollment found for this course section"));
    }

    private Student findStudent(Long orgId, String studentNumber) {
        return studentRepo.findByOrganisationIdAndStudentNumber(orgId, studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }

    private Quiz findQuiz(Long quizId, Long orgId) {
        return quizRepo.findByIdAndOrganisationId(quizId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found or belongs to another organisation"));
    }

}
