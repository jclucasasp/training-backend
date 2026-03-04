package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentQuizRepository extends JpaRepository<StudentQuiz, Long> {
        boolean existsByStudentAndQuiz(Student student, Quiz quiz);
}
