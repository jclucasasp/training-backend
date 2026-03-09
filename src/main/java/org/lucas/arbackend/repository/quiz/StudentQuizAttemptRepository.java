package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.StudentQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentQuizAttemptRepository extends JpaRepository<StudentQuizAttempt, Long> {

    List<StudentQuizAttempt> findAllByStudentIdAndOrganisationId(Long studentId, Long orgId);

     @Query("SELECT sqa FROM StudentQuizAttempt sqa " +
           "JOIN sqa.student s " +
           "WHERE sqa.id = :attemptId " +
           "AND s.studentNumber = :studentNumber " +
           "AND sqa.organisation.id = :orgId")
    Optional<StudentQuizAttempt> findByIdAndStudentNumber(
            @Param("attemptId") Long attemptId,
            @Param("studentNumber") String studentNumber,
            @Param("orgId") Long orgId);

    Collection<StudentQuizAttempt> findAllByOrganisationIdAndStudentIdAndQuizIdOrderByCompletedAtDesc(Long orgId, Long id, Long quizId);
}
