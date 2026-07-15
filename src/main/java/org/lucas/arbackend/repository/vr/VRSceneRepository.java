package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VRSceneRepository extends JpaRepository<VRScene, Long> {
    List<VRScene> findAllByOrganisationId(Long organisationId);
    Optional<VRScene> findByIdAndOrganisationId(Long id, Long organisationId);
}
