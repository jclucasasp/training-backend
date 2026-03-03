package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // This tells JPA: "When you find a student by number,
    // fetch their Organisation and ApiKey in the same JOIN query."
//    @EntityGraph(value = "student.org-apikey", type = EntityGraph.EntityGraphType.FETCH)
//    Page<Student> findByStudentNumber(String studentNumber, Pageable pageable);

     // Paginated student lookup per Organisation
    Page<Student> findAllByOrganisationId(Long orgId, Pageable pageable);

    // Fast lookup for student sign-in/redirect
    Optional<Student> findByOrganisationIdAndStudentNumber(Long orgId, String studentNumber);

    Optional<Student> findByOrganisationAndStudentNumber(Organisation org, String studentNumber);

    Optional<Student> findByStudentNumber(String studentNumber);
}
