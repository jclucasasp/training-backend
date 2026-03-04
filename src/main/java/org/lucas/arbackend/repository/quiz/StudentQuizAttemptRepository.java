package org.lucas.arbackend.repository.quiz;

import org.lucas.arbackend.entity.quiz.StudentQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentQuizAttemptRepository extends JpaRepository<StudentQuizAttempt, Long> {

    List<StudentQuizAttempt> findAllByStudentIdAndOrganisationId(Long studentId, Long orgId);

}
