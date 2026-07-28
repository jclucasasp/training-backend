package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<VRScene, Long> {
    Page<VRScene> findAllByOrganisationId(Long orgId, Pageable pageable);

    Optional<VRScene> findByIdAndOrganisationId(Long id, Long organisationId);
}
