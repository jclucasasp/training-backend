package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByIdAndOrganisationIdAndEndedAtIsNull(Long quizId, Long aLong);

    Optional<Quiz> findByIdAndOrganisationId(Long id, Long aLong);
}
