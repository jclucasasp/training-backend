package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // This tells JPA: "When you find a student by number,
    // fetch their Organisation and ApiKey in the same JOIN query."
//    @EntityGraph(value = "student.org-apikey", type = EntityGraph.EntityGraphType.FETCH)
//    Page<Student> findByStudentNumber(String studentNumber, Pageable pageable);

     // Paginated student lookup per Organisation
    Page<Student> findByOrganisationId(Long orgId, Pageable pageable);

    // Fast lookup for student sign-in/redirect
    Optional<Student> findByOrganisationIdAndStStudentNumber(Long orgId, String studentNumber);
}
