package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Optimized pagination for an Organisation's courses
    @EntityGraph(value = "Course.withChapterAndSections", type = EntityGraph.EntityGraphType.LOAD)
    Page<Course> findAllByOrganisationIdAndEndedAtIsNull(Long orgId, Pageable pageable);

    @EntityGraph(value = "Course.withChapterAndSections", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Course> findByIdAndOrganisationIdAndEndedAtIsNull(Long id, Long orgId);

    @EntityGraph(value = "Course.withChapterAndSections", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Course> findByOrganisationIdAndEndedAtIsNull(Long orgId);

}
