package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    // Used when editing a specific courseModule to ensure it belongs to the right course
    @Query("SELECT m FROM Chapter m WHERE m.id = :chapterId AND m.course.id = :courseId")
    Optional<Chapter> findByIdAndCourseId(@Param("chapterId") Long moduleId, @Param("courseId") Long courseId);

    Optional<Chapter> findByIdAndOrganisationId(Long chapterId, Long orgId);
}
