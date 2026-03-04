package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
     // Paginated student lookup per Organisation
    Page<Student> findAllByOrganisationId(Long orgId, Pageable pageable);

    // Fast lookup for student sign-in/redirect
    Optional<Student> findByOrganisationIdAndStudentNumber(Long orgId, String studentNumber);

    Optional<Student> findByOrganisationAndStudentNumber(Organisation org, String studentNumber);

    Optional<Student> findByStudentNumber(String studentNumber);

     @Query("SELECT s FROM Student s JOIN s.enrolledCourses c WHERE c.id = :courseId")
    Optional<List<Student>> findAllByEnrolledCourses(Long courseId);
}
