package org.lucas.arbackend.service.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.QuizAttemptResponse;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.quiz.StudentQuizAttempt;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.lucas.arbackend.mapper.StudentMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.quiz.StudentQuizAttemptRepository;
import org.lucas.arbackend.repository.student.StudentEnrollmentRepository;
import org.lucas.arbackend.repository.student.StudentProgressRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private final StudentQuizAttemptRepository attemptRepo;
    private final ObjectMapper objectMapper;
    private final StudentQuizAttemptRepository studentQuizAttemptRepo;


    // ==========================================
    // 1. ENROLLMENT LOGIC (UPSERT Student)
    // ==========================================
    // TODO: Add the student progress table as well.
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
        Course course = courseRepo.findByOrganisationIdAndSlug(org.getId(), request.getSlug())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Create Enrollment
        StudentEnrollment enrollment = enrollmentRepo.findByStudentIdAndCourseId(org.getId(), student.getId(), course.getId())
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

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentNumber(student.getStudentNumber())
                .courseName(course.getName())
                .enrolledAt(enrollment.getEnrolledAt())
                .currentTotalProgress(BigDecimal.ZERO)
                .build();
    }

    public void removeStudent(String studentNumber) {
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(tenantProvider.get(), studentNumber)
                        .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        studentRepo.delete(student);
    }

    // ==========================================
    // 2. PROGRESS TRACKING
    // ==========================================
    @Transactional
    public void updateProgress(String studentNumber, Long courseId, Long chapterId, Long sectionId, boolean isCompleted) {
        // 1. Context Resolution (Org & Student)
        Organisation org = findOrganisation();

        Student student = findStudent(org.getId(), studentNumber);

        // 2. Resolve the Section first (needed for both Enrollment lookup and Progress)
        Course course = courseRepo.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));

        ChapterSection chapterSection = course.getChapters().stream()
                .flatMap(c -> c.getChapterSections().stream())
                .filter(s -> s.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Section not found")
                );

        // 3. Find Enrollment (Scoped by Student and the Course this section belongs to)
        StudentEnrollment enrollment = findEnrollment(student.getId(), course.getId());

        // 5. Track the specific Section Progress
        handleSectionProgress(enrollment, chapterSection, org);

        BigDecimal total = calculateTotalProgress(enrollment, course);

        enrollment.setTotalProgress(total);

        // 6. Optional: Check if the whole Course is now 100% complete
        if (total.compareTo(BigDecimal.valueOf(100)) > 0 && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }
        enrollmentRepo.save(enrollment);
    }

//    private void handleSectionProgress(StudentEnrollment enrollment, ChapterSection chapterSection, Organisation org) {
//
//        int chapterTotalMinutes = chapterSection.getChapter().getTotalTimeInMinutes();
//        int sectionTotalMinutes = chapterSection.getChapter().getTotalTimeInMinutes();
//        BigDecimal percentage = calculatePercentage(sectionTotalMinutes, chapterTotalMinutes);
//
//        // Look for existing progress for this specific enrollment + section
//        StudentProgress progress = enrollment.getStudentProgresses().stream()
//                .filter(p -> p.getChapterSection().getId().equals(chapterSection.getId()))
//                .findFirst()
//                .orElseGet(() -> {
//                    StudentProgress newStudentProgress = StudentProgress.builder()
//                            .studentEnrollment(enrollment)
//                            .organisation(org)
//                            .chapter(chapterSection.getChapter())
//                            .chapterSection(chapterSection)
//                            .isCompleted(true)
//                            .percentage(percentage)
//                            .lastAccessedAt(LocalDateTime.now())
//                            .build();
//
//                    enrollment.getStudentProgresses().add(newStudentProgress);
//                    return newStudentProgress;
//                });
//
//        // Update percentage only if the new progress is higher (don't let progress go backwards)
//        if (percentage.compareTo(progress.getPercentage()) > 0) {
//            progress.setPercentage(percentage);
//        }
//
//        // Mark as completed if percentage hits 100 (or your specific threshold)
//        if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
//            progress.setIsCompleted(true);
//        }
//
//        progressRepo.save(progress);
//    }

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
    progressRepo.save(progress);

    // 6. NOW it is safe to calculate aggregated snapshots!
    // Since the database/collection is updated, you can safely compute overall metrics:
    BigDecimal finalCourseProgress = calculateTotalProgress(enrollment, enrollment.getCourse());
    enrollment.setTotalProgress(finalCourseProgress);

    if (finalCourseProgress.compareTo(BigDecimal.valueOf(100)) >= 0) {
        enrollment.setCompletedAt(LocalDateTime.now());
    }
    enrollmentRepo.save(enrollment);
}

    private BigDecimal calculateChapterProgress(StudentEnrollment enrollment, Chapter chapter) {
        List<StudentProgress> allSectionsInChapter = progressRepo
    .findByStudentEnrollmentIdAndChapterId(enrollment.getId(), chapter.getId());

int chapterTotalMinutes = chapter.getTotalTimeInMinutes();
int completedMinutesInChapter = allSectionsInChapter.stream()
        .filter(StudentProgress::getIsCompleted)
        .map(StudentProgress::getChapterSection)
        .filter(Objects::nonNull)
        .mapToInt(ChapterSection::getDurationInMinutes)
        .sum(); // Sum up the unique completed sections inside this chapter

// 3. This gives you your true, safely accumulated Chapter Progress percentage
       return calculatePercentage(completedMinutesInChapter, chapterTotalMinutes);
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
                .map(studentMapper::maptToStudentResponse);
    }

    private BigDecimal calculatePercentage(int sectionCompletedMinutes, int totalMinutes) {
        if (totalMinutes == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal completed = BigDecimal.valueOf(sectionCompletedMinutes);
        BigDecimal total = BigDecimal.valueOf(totalMinutes);

        return completed.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

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
    @Transactional(readOnly = true)
    public EnrollmentResponse getResumeDetails(String studentNumber, String courseSlug) {
        StudentEnrollment enrollment = findEnrollment(studentNumber, courseSlug);

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .courseName(enrollment.getCourse().getName())
                .currentTotalProgress(enrollment.getTotalProgress())
                // Provide the ID of the section they last viewed
                .build();
    }

    // TODO: Change to chapter section
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentDashboard(String studentNumber) {
        Long orgId = tenantProvider.get();
        return enrollmentRepo.findAllByStudentNumber(orgId, studentNumber)
                .stream()
                .map(e -> EnrollmentResponse.builder()
                        .enrollmentId(e.getId())
                        .studentNumber(studentNumber)
                        .courseName(e.getCourse().getName())
                        .currentTotalProgress(e.getTotalProgress()) // This saves us from having to map through the studentProgresses set on every view
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public String getAttemptReview(String studentNumber, Long attemptId) {
        StudentQuizAttempt attempt = attemptRepo.findByIdAndStudentNumber(attemptId, studentNumber, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found"));

        return attempt.getSubmittedAnswersJson();
    }

     @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getQuizAttempts(String studentNumber, Long quizId) {
        Long orgId = tenantProvider.get();

        // 1. Find the student in this Org
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(orgId, studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found in this organisation"));

        // 2. Fetch all attempts for this student/quiz combination
        // We use a specific repository method to ensure multi-tenant safety
        return attemptRepo.findRecentAttempts(
                orgId, student.getId(), quizId)
                .stream()
                .map(attempt -> QuizAttemptResponse.builder()
                        .attemptId(attempt.getId())
                        .quizId(attempt.getQuiz().getId())
                        .score(attempt.getScore())
                        .isPassed(attempt.isPassed())
                        .completedAt(attempt.getCompletedAt())
                        // We leave 'answers' null here to keep the list response small
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getAttemptDetails(Long attemptId) {
        Long currentOrgId = tenantProvider.get();

        StudentQuizAttempt attempt = attemptRepo.findById(attemptId)
                .filter(a -> a.getOrganisation().getId().equals(currentOrgId))
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found or access denied"));

        Object parsedAnswers = null;
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

    private String convertToJson(Object answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private Organisation findOrganisation() {
        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
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

    private StudentQuiz findStudentQuiz(String studentNumber, Long quizId){
        return studentQuizRepo.findRegistration(
                        tenantProvider.get(), studentNumber, quizId)
                .orElseThrow(() -> new EntityNotFoundException("Student is not registered for this quiz"));
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
