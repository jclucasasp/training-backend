package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterQuizRepository extends JpaRepository<ChapterQuiz, Long> {
}
