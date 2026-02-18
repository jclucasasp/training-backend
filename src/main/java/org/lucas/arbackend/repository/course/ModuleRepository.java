package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<CourseChapter, Long> {
    // Used when editing a specific courseModule to ensure it belongs to the right course
    @Query("SELECT m FROM Module m WHERE m.id = :moduleId AND m.course.id = :courseId")
    Optional<CourseChapter> findByIdAndCourseId(@Param("moduleId") Long moduleId, @Param("courseId") Long courseId);
}
