package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.competency.Competency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    Optional<Competency> findByIdAndOrganisationId(Long id, Long organisationId);
    Page<Competency> findAllByOrganisationId(Long organisationId, Pageable pageable);
    List<Competency> findAllByAssociatedSceneIdAndOrganisationId(Long sceneId, Long organisationId);
}
