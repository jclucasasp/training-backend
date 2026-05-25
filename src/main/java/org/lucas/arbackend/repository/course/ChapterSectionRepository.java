package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterSectionRepository extends JpaRepository<ChapterSection, Long> {

    Long countByChapterCourse(Course course);

    @Query("SELECT s FROM ChapterSection s " +
           "JOIN FETCH s.chapter c " +
           "JOIN FETCH c.course co " +
           "WHERE s.id = :sectionId AND c.id = :chapterId AND co.id = :courseId")
    Optional<ChapterSection> findWithContext(
        @Param("courseId") Long courseId,
        @Param("chapterId") Long chapterId,
        @Param("sectionId") Long sectionId
    );
}
