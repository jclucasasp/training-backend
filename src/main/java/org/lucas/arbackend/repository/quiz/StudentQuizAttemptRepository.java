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

     @Query("SELECT sqa FROM student_quiz_attempts WHERE sqa.organisation_id = :orgId AND sqa.student_id = :studentId AND sqa.quiz_id = :quizId ORDER BY DESC")
    List<StudentQuizAttempt> findRecentAttempts(@Param("orgId") Long orgId, @Param("studentId") Long studentId, @Param("quizId") Long quizId);
}
