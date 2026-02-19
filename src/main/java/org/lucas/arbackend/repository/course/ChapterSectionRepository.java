package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ChapterSectionRepository extends JpaRepository<ChapterSection, Long> {

    Long countByChapterCourse(Course course);
}
