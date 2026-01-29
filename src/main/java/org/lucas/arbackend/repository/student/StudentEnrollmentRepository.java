package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {
    @EntityGraph(attributePaths = {"studnet", "course"})
    List<StudentEnrollment> findbyStudent_StudentNumber(String studentNumber);
}
