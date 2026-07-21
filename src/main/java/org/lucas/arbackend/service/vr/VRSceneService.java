package org.lucas.arbackend.service.vr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.asset.VRAssetCreateRequest;
import org.lucas.arbackend.dto.vr.asset.VRAssetResponse;
import org.lucas.arbackend.dto.vr.scene.VRSceneResolutionResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.vr.asset.VRAsset;
import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.vr.VRSceneVersionRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VRSceneService {
    private final ChapterSectionRepository sectionRepo;
    private final VRSceneVersionRepository versionRepo;
    private final TenantProvider tenantProvider;

    @Transactional(readOnly = true)
    public VRSceneResolutionResponse resolveActiveSceneForSection(Long sectionId) {
        ChapterSection section = sectionRepo.findByIdAndOrganisationId(sectionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Chapter section not found"));

        VRScene scene = section.getVrScene();
        if (scene == null) {
            throw new IllegalStateException("This training section does not have an associated VR Scene configured.");
        }

        VRSceneVersion activeVersion = versionRepo.findAllBySceneId(scene.getId())
                .stream()
                .filter(VRSceneVersion::isActive)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No active VR Scene version found for this scene."));

        return VRSceneResolutionResponse.builder()
                .sectionId(section.getId())
                .sceneId(scene.getId())
                .sceneVersionId(activeVersion.getId())
                .versionTag(activeVersion.getVersionTag())
                .environmentalFileUrl(activeVersion.getEnvironmentalFileUrl())
                .hierarchyJson(activeVersion.getHierarchyJson())
                .build();
    }


}
