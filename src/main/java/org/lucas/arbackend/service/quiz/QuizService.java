package org.lucas.arbackend.service.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.quiz.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.*;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.mapper.QuizMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterQuizRepository;
import org.lucas.arbackend.repository.course.ChapterRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.quiz.StudentQuizAttemptRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {
    private final StudentQuizAttemptRepository attemptRepo;
    private final StudentQuizRepository studentQuizRepo;
    private final StudentRepository studentRepo;
    private final TenantProvider tenantProvider;
    private final ChapterRepository chapterRepo;
    private final ChapterQuizRepository chapterQuizRepo;
    private final CourseRepository courseRepo;
    private final ObjectMapper objectMapper;
    private final QuizRepository quizRepo;
    private final QuizMapper quizMapper;

    // ==========================================
    // CORE QUIZ OPERATIONS
    // ==========================================

/**
 * Creates a new quiz based on the provided request and creator information.
 * This method handles the entire process of quiz creation, including validation,
 * mapping, linking to course/chapter, and saving to the repository.
 *
 * @param request The QuizRequest containing all necessary information for quiz creation
 * @param creator The Staff member who is creating the quiz
 * @return QuizResponse representing the newly created quiz
 * @throws EntityNotFoundException If the specified course doesn't exist or access is denied
 */
// TODO: Need to save the created quiz to the chapter_quizzes table
    public QuizResponse createQuiz(QuizRequest request, Staff creator) {
    // Get the organization ID from the tenant provider
        Long orgId = tenantProvider.get();

    // Find the course by ID and organization ID, throw exception if not found
        Course course = courseRepo.findByIdAndOrganisationId(request.getCourseId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

    // Create mapping context with organization, null for parent, and creator
        MappingContext ctx = new MappingContext(creator.getOrganisation(), null, creator);
    // Convert request to quiz entity using mapper with the created context
        Quiz quiz = quizMapper.toEntity(request, ctx);
    // Set the course and organisation for the quiz
        quiz.setOrganisation(creator.getOrganisation());
        quiz.setCourse(course);

    // Wire the quiz hierarchy structure
        wireQuizHierarchy(quiz, creator);

    // If chapter ID is provided, link the quiz to the chapter
        if (request.getCourseId() != null && request.getChapterId() != null) {
            linkToChapter(quiz, request.getCourseId(), request.getChapterId(), orgId);
        }

    // Save the quiz to repository and convert to response
        return quizMapper.toResponse(quizRepo.save(quiz));
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long quizId) {
        return quizMapper.toResponse(getQuiz(quizId));
    }

    public QuizResponse updateQuizMetadata(Long quizId, QuizRequest request) {
        Quiz quiz = getQuiz(quizId);

        quiz.setTitle(request.getTitle());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setPassingScore(request.getPassingScore());
        // courseId change is usually not allowed after creation to maintain data integrity

        return quizMapper.toResponse(quizRepo.save(quiz));
    }

    // ==========================================
    // ASSIGNMENT OPERATIONS
    // ==========================================

    public void assignQuizToChapter(Long quizId, Long courseId, Long chapterId) {
        Quiz quiz = getQuiz(quizId);
        linkToChapter(quiz, courseId, chapterId, tenantProvider.get());
        quizRepo.save(quiz);
    }
    public void assignQuizToChapter(Quiz quiz, Chapter chapter) {
         log.info("Creating new audit link for quiz: [{}] to new chapter: [{}]", quiz.getId(), chapter.getId());
        if (quiz.getChapterQuizzes() != null) {
           ChapterQuiz link = ChapterQuiz.builder()
            .quiz(quiz) // @MapsId will take this ID
            .chapter(chapter)
            .organisation(quiz.getOrganisation())
            .build();

            chapter.getChapterQuizzes().add(link);
            quiz.getChapterQuizzes().add(link);

            chapterQuizRepo.saveAndFlush(link);
            log.info("DEBUG: ChapterQuiz link saved with ID: {}", link.getId());
        }
    }


    public void assignQuizToEnrolledStudents(Long quizId, Long courseId) {
        Quiz quiz = getQuiz(quizId);
        Long orgId = tenantProvider.get();

        // Ensure course belongs to org
        if (!courseRepo.existsByIdAndOrganisationId(courseId, orgId)) {
            throw new AccessDeniedException("Course not found in your organisation");
        }

        List<Student> enrolledStudents = studentRepo.findAllByEnrolledCourses(orgId, courseId);

        List<StudentQuiz> assignments = enrolledStudents.stream()
                .filter(student -> !studentQuizRepo.existsByStudentIdAndQuizId(student.getId(), quizId))
                .map(student -> StudentQuiz.builder()
                        .student(student)
                        .quiz(quiz)
                        .organisation(quiz.getOrganisation())
                        .build())
                .toList();

        studentQuizRepo.saveAll(assignments);
    }

    // ==========================================
    // QUESTION MANAGEMENT
    // ==========================================

    public void addQuestionToQuiz(Long quizId, QuestionRequest request) {
        Quiz quiz = getQuiz(quizId);
        MappingContext ctx = new MappingContext(quiz.getOrganisation(), null, null);

        QuizQuestion question = quizMapper.toQuestionEntity(request, ctx);
        question.setQuiz(quiz);

        question.getOptions().forEach(o -> {
            o.setQuestion(question);
            o.setOrganisation(quiz.getOrganisation());
        });

        quiz.getQuestions().add(question);
        quizRepo.save(quiz);
    }

    public void updateQuestion(Long quizId, Long questionId, QuestionRequest request) {
        Quiz quiz = getQuiz(quizId);
        QuizQuestion question = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Question not found in this quiz"));

        question.setText(request.getText());
        question.getOptions().clear(); // Relies on orphanRemoval = true in Entity

        MappingContext ctx = new MappingContext(quiz.getOrganisation(), null, null);
        request.getOptions().forEach(optDto -> {
            QuizQuestionOption option = quizMapper.toOptionEntity(optDto, ctx);
            option.setQuestion(question);
            question.getOptions().add(option);
        });

        quizRepo.save(quiz);
    }

    public void removeQuestion(Long quizId, Long questionId) {
        Quiz quiz = getQuiz(quizId);
        boolean removed = quiz.getQuestions().removeIf(q -> q.getId().equals(questionId));

        if (!removed) {
            throw new EntityNotFoundException("Question not found in this quiz");
        }
        quizRepo.save(quiz);
    }

    // ==========================================
    // GRADING LOGIC
    // ==========================================

    public QuizResultResponse submitAndGradeQuiz(String studentNumber, Long quizId, QuizSubmissionRequest submission) {
        StudentQuiz studentQuiz = findStudentQuiz(studentNumber, quizId);
        Quiz quiz = studentQuiz.getQuiz();

        // A. Check Attempt Limit
        long existingAttempts = attemptRepo.countByStudentIdAndQuizId(studentQuiz.getStudent().getId(), quizId);
        if (existingAttempts >= quiz.getMaxAttempts()) {
            throw new IllegalStateException("Maximum attempts reached for this quiz.");
        }

        // B. Prepare Grading Map for Speed and Security
        Map<Long, QuizQuestion> quizMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        int correctAnswers = 0;
        Set<Long> processedQuestions = new HashSet<>();

        // C. Secure Grading Loop
        for (AnswerDTO submitted : submission.getAnswers()) {
            Long qId = submitted.getQuestionId();

            // Ignore duplicates or questions not belonging to this quiz
            if (processedQuestions.contains(qId) || !quizMap.containsKey(qId)) continue;

            QuizQuestion question = quizMap.get(qId);
            boolean isCorrect = question.getOptions().stream()
                    .anyMatch(opt -> opt.getId().equals(submitted.getSelectedOptionId()) && opt.isCorrect());

            if (isCorrect) correctAnswers++;
            processedQuestions.add(qId);
        }

        // D. Calculate Final Score
        BigDecimal score = BigDecimal.valueOf((correctAnswers / (double) quizMap.size()) * 100)
                .setScale(2, RoundingMode.HALF_UP);

        // E. Persist Attempt
        StudentQuizAttempt attempt = StudentQuizAttempt.builder()
                .organisation(studentQuiz.getOrganisation())
                .student(studentQuiz.getStudent())
                .quiz(quiz)
                .score(score)
                .submittedAnswersJson(convertToJson(submission.getAnswers()))
                .isPassed(score.compareTo(BigDecimal.valueOf(quiz.getPassingScore())) >= 0)
                .completedAt(LocalDateTime.now())
                .build();

        StudentQuizAttempt quizAttempt = attemptRepo.save(attempt);
        return new QuizResultResponse(quizAttempt.getId(), score, attempt.isPassed());
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private Quiz getQuiz(Long quizId) {
        return quizRepo.findByIdAndOrganisationId(quizId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    private void wireQuizHierarchy(Quiz quiz, Staff creator) {
        if (quiz.getQuestions() == null) return;

        int index = 0;
        for (QuizQuestion q : quiz.getQuestions()) {
            q.setOrderIndex(index);
            q.setQuiz(quiz);
            q.setOrganisation(creator.getOrganisation());
            if (q.getOptions() != null) {
                q.getOptions().forEach(o -> {
                    o.setQuestion(q);
                    o.setOrganisation(creator.getOrganisation());
                });
            }
            index ++;
        }

    }

    private void linkToChapter(Quiz quiz, Long courseId, Long chapterId, Long orgId ) {
        log.info("DEBUG: Incoming request to link quiz: [{}] to course: [{}] for chapter: [{}]", quiz.getId(), courseId, chapterId);

        Course course = courseRepo.findByIdAndOrganisationId(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Now course found for organisation id: [" + orgId + "]"));

        Chapter chapter = course.getChapters().stream()
                .filter(c -> c.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

        if (quiz.getChapterQuizzes() != null) {
            log.info("Quizzes found for the chapter, checking if it already exist so no duplication happens...");
        // Check if link already exists to prevent duplicates
        boolean exists = quiz.getChapterQuizzes().stream()
                .anyMatch(cq -> cq.getChapter().getId().equals(chapterId));

            if (exists){
                log.info("Link already exists for quiz: [{}] to chapter: [{}]", quiz.getId(), chapterId);
                return;
            }

            log.info("DEBUG: No link exist, creating...");
            ChapterQuiz link = ChapterQuiz.builder()
                    .quiz(quiz)
                    .chapter(chapter)
                    .organisation(quiz.getOrganisation())
                    .build();
            quiz.getChapterQuizzes().add(link);

            chapterQuizRepo.save(link);

            chapter.getChapterQuizzes().add(link);
//            chapterRepo.save(chapter);
        }

    }

    private StudentQuiz findStudentQuiz(String studentNumber, Long quizId) {
        return studentQuizRepo.findRegistration(tenantProvider.get(), studentNumber, quizId)
                .orElseThrow(() -> new EntityNotFoundException("Student not registered for this quiz"));
    }

    private String convertToJson(Set<AnswerDTO> answers) {
        String valueAsString;

        try {
            valueAsString = objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialise quiz answers", e);
        }
        return valueAsString;
    }
}