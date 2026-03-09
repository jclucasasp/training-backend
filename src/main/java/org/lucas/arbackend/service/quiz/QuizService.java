package org.lucas.arbackend.service.quiz;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.QuizRequest;
import org.lucas.arbackend.dto.quiz.QuizResponse;
import org.lucas.arbackend.dto.quiz.QuizResultResponse;
import org.lucas.arbackend.dto.quiz.QuizSubmission;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.QuizQuestion;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.quiz.StudentQuizAttempt;
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

    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepo.findByIdAndOrganisationId(id, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
        return quizMapper.toResponse(quiz);
    }

    public QuizResultResponse submitAttempt(Long quizId, Student student, QuizSubmission submission) {
        Quiz quiz = quizRepo.findByIdAndOrganisationId(quizId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

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
    Quiz quiz = quizRepo.findByIdAndOrganisationId(quizId, tenantProvider.get())
            .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

    // Assuming you have a ChapterRepository
    Chapter chapter = chapterRepo.findById(chapterId)
            .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

    ChapterQuiz assignment = ChapterQuiz.builder()
            .quiz(quiz)
            .chapter(chapter)
            .organisation(quiz.getOrganisation())
            .build();

    chapterQuizRepo.save(assignment);
}

    public void assignQuizToEnrolledStudents(Long quizId, Long courseId) {
    Quiz quiz = quizRepo.findByIdAndOrganisationId(quizId, tenantProvider.get())
            .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

    // Fetch students enrolled in the course
    List<Student> enrolledStudents = studentRepo.findAllByEnrolledCourses(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found"));

    List<StudentQuiz> assignments = enrolledStudents.stream()
        .map(student -> StudentQuiz.builder()
            .student(student)
            .quiz(quiz)
            .organisation(quiz.getOrganisation())
            .build())
        .toList();

    studentQuizRepo.saveAll(assignments);
    }

}
