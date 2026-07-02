package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.VRSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VRSessionRepository extends JpaRepository<VRSession, Long> {
@EntityGraph(attributePaths = {"student", "chapterSection", "organisation"})
    Optional<VRSession> findByIdAndOrganisationId(Long id, Long orgId);

    @EntityGraph(attributePaths = {"student", "chapterSection"})
    Page<VRSession> findAllByStudentStudentNumberAndOrganisationId(String studentNumber, Long orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "chapterSection"})
    Page<VRSession> findAllByOrganisationId(Long orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"student"})
    List<VRSession> findAllByStudentStudentNumberAndOrganisationId(String studentNumber, Long orgId);

    @Query("SELECT COUNT(v) FROM VRSession v WHERE v.student.studentNumber = :studentNumber AND v.organisation.id = :orgId AND v.completionConditionMet = true")
    long countCompletedSessions(@Param("studentNumber") String studentNumber, @Param("orgId") Long orgId);

    @Query("SELECT AVG(v.sessionQualityScore) FROM VRSession v WHERE v.student.studentNumber = :studentNumber AND v.organisation.id = :orgId")
    Double calculateAverageQualityScore(@Param("studentNumber") String studentNumber, @Param("orgId") Long orgId);

    @Query("SELECT COUNT(v) FROM VRSession v WHERE v.student.studentNumber = :studentNumber AND v.organisation.id = :orgId AND v.motionSicknessReported = true")
    long countMotionSicknessReports(@Param("studentNumber") String studentNumber, @Param("orgId") Long orgId);

}
