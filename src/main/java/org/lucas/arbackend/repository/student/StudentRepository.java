package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
     // Paginated student lookup per Organisation
    Page<Student> findAllByOrganisationId(Long orgId, Pageable pageable);

    // Fast lookup for student sign-in/redirect
    Optional<Student> findByOrganisationIdAndStudentNumber(Long orgId, String studentNumber);


    @Query("SELECT e.student FROM StudentEnrollment e WHERE e.organisation.id = :orgId AND e.course.id = :courseId")
    List<Student> findAllByEnrolledCourses(@Param("orgId") Long orgId, @Param("courseId") Long courseId);
}
