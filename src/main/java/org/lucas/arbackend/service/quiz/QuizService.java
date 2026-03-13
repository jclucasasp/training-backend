package org.lucas.arbackend.service.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.*;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.mapper.QuizMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.quiz.StudentQuizAttemptRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.util.TenantProvider;
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

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {
    private final QuizRepository quizRepo;
    private final StudentQuizAttemptRepository attemptRepo;
    private final QuizMapper quizMapper;
    private final TenantProvider tenantProvider;
    private final ChapterRepository chapterRepo;
    private final StudentQuizRepository studentQuizRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final ObjectMapper objectMapper;

    // ==========================================
    // CORE QUIZ OPERATIONS
    // ==========================================

    public QuizResponse createQuiz(QuizRequest request, Staff creator) {
        Long orgId = tenantProvider.get();

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(request.getCourseId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found or access denied"));

        MappingContext ctx = new MappingContext(creator.getOrganisation(), null, creator);
        Quiz quiz = quizMapper.toEntity(request, ctx);
        quiz.setCourse(course);

        wireQuizHierarchy(quiz, creator);

        if (request.getChapterId() != null) {
            linkToChapter(quiz, request.getChapterId(), orgId);
        }

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

    public void assignQuizToChapter(Long quizId, Long chapterId) {
        Quiz quiz = getQuiz(quizId);
        linkToChapter(quiz, chapterId, tenantProvider.get());
        quizRepo.save(quiz);
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
        quiz.getQuestions().forEach(q -> {
            q.setQuiz(quiz);
            q.setOrganisation(creator.getOrganisation());
            if (q.getOptions() != null) {
                q.getOptions().forEach(o -> {
                    o.setQuestion(q);
                    o.setOrganisation(creator.getOrganisation());
                });
            }
        });
    }

    private void linkToChapter(Quiz quiz, Long chapterId, Long orgId) {
        Chapter chapter = chapterRepo.findByIdAndOrganisationId(chapterId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

        // Check if link already exists to prevent duplicates
        boolean exists = quiz.getChapterQuizzes().stream()
                .anyMatch(cq -> cq.getChapter().getId().equals(chapterId));

        if (!exists) {
            ChapterQuiz link = ChapterQuiz.builder()
                    .quiz(quiz)
                    .chapter(chapter)
                    .organisation(quiz.getOrganisation())
                    .build();
            quiz.getChapterQuizzes().add(link);
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