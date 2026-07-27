package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.competency.CompetencyAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetencyAssessmentRepository extends JpaRepository<CompetencyAssessment, Long> {
    List<CompetencyAssessment> findByStudent_StudentNumberOrderByAssessedAtDesc(String studentNumber);
    Page<CompetencyAssessment> findByStudent_StudentNumber(@Param("studentNumber") String studentNumber, Pageable pageable);

    long countByStudent_StudentNumber(String studentNumber);
    long countByStudent_StudentNumberAndPassedTrue(String studentNumber);
}
