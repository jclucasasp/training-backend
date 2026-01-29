package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.student.StudentProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    @EntityGraph(attributePaths = {"enrollment", "module"})
    List<StudentProgress> findByEnrollement_Id(Long enrollmentId);
}
