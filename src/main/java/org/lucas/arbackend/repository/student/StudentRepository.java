package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.student.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // This tells JPA: "When you find a student by number,
    // fetch their Organisation and ApiKey in the same JOIN query."
    @EntityGraph(attributePaths = {"organisation", "apiKey"})
    Optional<Student> findByStudentNumber(String studentNumber);

    Collection<Student> findAllByOrganisationId(Long orgId);

    Optional<Student> findByIdAndOrganisationId(Long studentId, Long orgId);

    Optional<Student> findByStudentNumberAndOrganisationId(String studentNumber, Long id);
}
