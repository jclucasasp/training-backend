package org.lucas.arbackend.repository.quiz;

import org.hibernate.annotations.processing.SQL;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(value = "Quiz.questionsAndOptions", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Quiz> findByIdAndOrganisationId(Long id, Long orgId);

    @EntityGraph(value = "Quiz.questionsAndOptions", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Quiz> findByCourseIdAndOrganisationId(Long courseId, Long orgId);
}
