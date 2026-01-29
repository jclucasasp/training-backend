package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByName(String courseName);

    // Enforces that only courses linked to the specific Org are returned
    @Query("SELECT c FROM Course c JOIN OrgCourseRel ocr ON c.id = ocr.course.id WHERE ocr.org.id = :orgId")
    List<Course> findAllByOrganisationId(@Param("orgId") Long orgId);
}
