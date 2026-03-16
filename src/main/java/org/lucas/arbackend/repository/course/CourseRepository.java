package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Optimized pagination for an Organisation's courses
    Page<Course> findAllByOrganisationId(Long orgId, Pageable pageable);

    @EntityGraph(value = "Course.withChapterAndSections", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Course> findByIdAndOrganisationId(Long id, Long orgId);

    @EntityGraph(value = "Course.withChapterAndSections", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Course> findByOrganisationIdAndSlug(Long orgId, String slug);

    boolean existsByIdAndOrganisationId(Long courseId, Long orgId);

}
