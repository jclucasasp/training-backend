package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(value = "Quiz.questionsAndOptions", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Quiz> findByIdAndOrganisationIdAndEndedAtIsNull(Long quizId, Long orgId);

    @EntityGraph(value = "Quiz.questionsAndOptions", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Quiz> findByIdAndOrganisationId(Long id, Long orgId);
}
