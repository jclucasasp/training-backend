package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.misc.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByIdAndOrganisationIdAndEndedAtIsNull(Long quizId, Long aLong);
}
