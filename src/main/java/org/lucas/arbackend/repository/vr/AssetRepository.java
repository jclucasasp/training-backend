package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.asset.VRAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<VRAsset, Long> {
    Optional<VRAsset> findByIdAndOrganisationId(Long id, Long organisationId);
}
