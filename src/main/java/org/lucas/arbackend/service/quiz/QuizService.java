package org.lucas.arbackend.service.quiz;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.quiz.*;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.mapper.QuizMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterQuizRepository;
import org.lucas.arbackend.repository.course.ChapterRepository;
import org.lucas.arbackend.repository.course.StudentQuizRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.quiz.StudentQuizAttemptRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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
    private final ChapterQuizRepository chapterQuizRepo;

    public QuizResponse createQuiz(QuizRequest request, Staff creator) {
        MappingContext ctx = new MappingContext(creator.getOrganisation(), null, creator);
        Quiz quiz = quizMapper.toEntity(request, ctx);

        // Manual wiring for JPA Parent-Child relationship
        quiz.getQuestions().forEach(q -> {
            q.setQuiz(quiz);
            q.getOptions().forEach(o -> o.setQuestion(q));
        });
        // Maps the chapter quizzes
        if (request.getChapterId() != null) {
        ChapterQuiz link = ChapterQuiz.builder()
            .quiz(quiz)
            .chapter(chapterRepo.getReferenceById(request.getChapterId()))
            .organisation(creator.getOrganisation())
            .build();

        quiz.getChapterQuizzes().add(link);
    }

        return quizMapper.toResponse(quizRepo.save(quiz));
    }

    public QuizResponse getQuizById(Long quizId) {
        Quiz quiz = getQuiz(quizId);
        return quizMapper.toResponse(quiz);
    }

    public QuizResultResponse submitAttempt(Long quizId, Student student, QuizSubmission submission) {
        Quiz quiz = getQuiz(quizId);

        int total = quiz.getQuestions().size();
        long correct = 0;

        for (QuizQuestion q : quiz.getQuestions()) {
            Long studentAnswerId = submission.answers().get(q.getId());
            boolean isCorrect = q.getOptions().stream()
                    .anyMatch(o -> o.getId().equals(studentAnswerId) && o.isCorrect());
            if (isCorrect) correct++;
        }

        BigDecimal score = BigDecimal.valueOf((correct / (double) total) * 100)
                .setScale(2, RoundingMode.HALF_UP);

        StudentQuizAttempt attempt = StudentQuizAttempt.builder()
                .organisation(student.getOrganisation())
                .student(student)
                .quiz(quiz)
                .score(score)
                .isPassed(score.compareTo(BigDecimal.valueOf(quiz.getPassingScore())) >= 0)
                .completedAt(LocalDateTime.now())
                .build();

        attemptRepo.save(attempt);
        return new QuizResultResponse(score, attempt.isPassed());
    }

    public void assignQuizToChapter(Long quizId, Long chapterId) {
    Quiz quiz = getQuiz(quizId);

    // Assuming you have a ChapterRepository
    Chapter chapter = chapterRepo.findByIdAndOrganisationId(chapterId, tenantProvider.get())
            .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

    ChapterQuiz assignment = ChapterQuiz.builder()
            .quiz(quiz)
            .chapter(chapter)
            .organisation(quiz.getOrganisation())
            .build();

    chapterQuizRepo.save(assignment);
}

    public void addQuestionToQuiz(Long quizId, QuestionRequest request) {
        Quiz quiz = getQuiz(quizId); // Already uses tenantProvider.get()

        // Map the DTO to Question Entity
        // Using a manual mapping here or a mapper method
        QuizQuestion question = QuizQuestion.builder()
                .text(request.getText())
                .quiz(quiz)
                .organisation(quiz.getOrganisation())
                .build();

        // Map and link options
        request.getOptions().forEach(opt -> {
            QuizQuestionOption option = QuizQuestionOption.builder()
                    .text(opt.getText())
                    .isCorrect(opt.isCorrect())
                    .question(question)
                    .build();
            question.getOptions().add(option);
        });

        quiz.getQuestions().add(question);
        quizRepo.save(quiz); // Cascades to the new question and options
    }

    public void updateQuestion(Long quizId, Long questionId, QuestionRequest request) {
        // 1. Fetch the quiz to verify ownership/tenant
        Quiz quiz = getQuiz(quizId);

        // 2. Find the specific question
        QuizQuestion question = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Question not found in this quiz"));

        // 3. Update fields
        question.setText(request.getText());

        // 4. Update Options (Cleanest way: Clear and re-add if the list is small)
        question.getOptions().clear();
        request.getOptions().forEach(opt -> {
            question.getOptions().add(QuizQuestionOption.builder()
                    .text(opt.getText())
                    .isCorrect(opt.isCorrect())
                    .question(question)
                    .build());
        });

        quizRepo.save(quiz);
    }

    public void removeQuestion(Long quizId, Long questionId) {
        Quiz quiz = getQuiz(quizId);

        boolean removed = quiz.getQuestions().removeIf(q -> q.getId().equals(questionId));

        if (!removed) {
            throw new EntityNotFoundException("Question not found");
        }

        quizRepo.save(quiz);
    }

    public QuizResponse updateQuizMetadata(Long quizId, QuizRequest request) {
        Quiz quiz = getQuiz(quizId);

        quiz.setTitle(request.getTitle());
        quiz.setPassingScore(request.getPassingScore());
        // Update other metadata fields as needed

        return quizMapper.toResponse(quizRepo.save(quiz));
    }

    public void assignQuizToEnrolledStudents(Long quizId, Long courseId) {
    Quiz quiz = getQuiz(quizId);

    // Fetch students enrolled in the course
    List<Student> enrolledStudents = getEnrolledStudents(courseId);

    List<StudentQuiz> assignments = enrolledStudents.stream()
        .map(student -> StudentQuiz.builder()
            .student(student)
            .quiz(quiz)
            .organisation(quiz.getOrganisation())
            .build())
        .toList();

    studentQuizRepo.saveAll(assignments);
    }

    private Quiz getQuiz(Long quizId) {
        return quizRepo.findByIdAndOrganisationId(quizId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    private List<Student> getEnrolledStudents(Long courseId) {
        List<Student> students = studentRepo.findAllByEnrolledCourses(tenantProvider.get(), courseId);
        if (students.isEmpty()) {
            throw new EntityNotFoundException("No students enrolled in the course");
        }

        return students;
    }

}
