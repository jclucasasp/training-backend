package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentQuizRepository extends JpaRepository<StudentQuiz, Long> {
        boolean existsByStudentAndQuiz(Student student, Quiz quiz);

        @Query("SELECT sq from StudentQuiz sq WHERE sq.student.organisation.id = :orgId AND sq.student.studentNumber = :studentNumber AND sq.quiz.id = :quizId")
        Optional<StudentQuiz> findRegistration(@Param("orgId") Long orgId, @Param(("studentNumber")) String studentNumber, @Param("quizId") Long quizId);

    boolean existsByStudentIdAndQuizId(Long id, Long quizId);
}
