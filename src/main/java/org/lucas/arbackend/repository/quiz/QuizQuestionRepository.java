package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query("SELECT qq FROM QuizQuestion qq JOIN qq.quiz q " +
            "WHERE qq.id = :questionId AND q.organisation.id = :orgId")
    Optional<QuizQuestion> findByIdAndOrganisationId(Long questionId, Long orgId);

    @Query("SELECT qq FROM QuizQuestion qq JOIN qq.quiz q " +
            "WHERE q.id = :quizId AND q.organisation.id = :orgId")
    List<QuizQuestion> findAllByQuizIdAndOrganisationId(Long quizId, Long orgId);
}
