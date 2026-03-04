package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.QuizQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizQuestionOptionRepository extends JpaRepository<QuizQuestionOption, Long> {
}
