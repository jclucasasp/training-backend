package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Optimized pagination for an Organisation's courses
    Page<Course> findByOrganisationIdAndEndedAtIsNull(Long orgId, Pageable pageable);

    // Optimized join fetch for the "Course Player" view
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.modules m LEFT JOIN FETCH m.sections WHERE c.id = :id")
    Optional<Course> findFullCourseTree(@Param("id") Long id);
}
