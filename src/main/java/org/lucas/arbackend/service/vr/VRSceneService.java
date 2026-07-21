package org.lucas.arbackend.service.vr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.scene.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;
import org.lucas.arbackend.mapper.context.VRSceneMapper;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.vr.VRSceneRepository;
import org.lucas.arbackend.repository.vr.VRSceneVersionRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VRSceneService {
    private final ChapterSectionRepository sectionRepo;
    private final VRSceneVersionRepository versionRepo;
    private final VRSceneRepository sceneRepo;
    private final TenantProvider tenantProvider;
    private final VRSceneMapper sceneMapper;

    @Transactional(readOnly = true)
    public VRSceneResolutionResponse resolveActiveSceneForSection(Long sectionId) {
        ChapterSection section = sectionRepo.findByIdAndOrganisationId(sectionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Chapter section not found"));

        VRScene scene = section.getVrScene();
        if (scene == null) {
            throw new IllegalStateException("This training section does not have an associated VR Scene configured.");
        }

        VRSceneVersion activeVersion = versionRepo.findBySceneIdAndIsActiveTrue(scene.getId())
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

    public VRSceneResponse createScene(VRSceneRequest request) {
        Organisation org = tenantProvider.getOrg();
        VRScene scene = VRScene.builder()
                .organisation(org)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return sceneMapper.toResponse(sceneRepo.save(scene));
    }

    public VRSceneResponse updateScene(Long sceneId, VRSceneRequest request) {
        VRScene scene = sceneRepo.findByIdAndOrganisationId(sceneId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("VR Scene not found"));

        scene.setTitle(request.getTitle());
        scene.setDescription(request.getDescription());

        return sceneMapper.toResponse(sceneRepo.save(scene));
    }

    @Transactional(readOnly = true)
    public VRSceneVersionResponse getSceneWithActiveHierarchy(Long sceneId) {
        sceneRepo.findByIdAndOrganisationId(sceneId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("VR Scene not found"));

        VRSceneVersion activeVersion = versionRepo.findBySceneIdAndIsActiveTrue(sceneId)
                .orElseThrow(() -> new EntityNotFoundException("No active VR Scene version found for this scene."));

        return sceneMapper.toVersionResponse(activeVersion);
    }

    public VRSceneVersionResponse createVersion(Long sceneId, VRSceneVersionRequest request) {
        VRScene scene = sceneRepo.findByIdAndOrganisationId(sceneId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("VR Scene not found"));

        if (request.getIsActive()) {
            versionRepo.deactivateAllVersionsForScene(sceneId);
        }

        VRSceneVersion version = VRSceneVersion.builder()
                .scene(scene)
                .versionTag(request.getVersionTag())
                .isActive(request.getIsActive())
                .environmentalFileUrl(request.getEnvironmentalFileUrl())
                .hierarchyJson(request.getHierarchyJson())
                .build();

        return sceneMapper.toVersionResponse(versionRepo.save(version));
    }

    @Transactional(readOnly = true)
    public Page<VRSceneVersionResponse> getAllVersionsForScene(Long sceneId, Pageable pageable) {
        sceneRepo.findByIdAndOrganisationId(sceneId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("VR Scene not found"));

        return versionRepo.findAllBySceneId(sceneId, pageable)
                .map(sceneMapper::toVersionResponse);
    }

    public VRSceneVersionResponse activateVersion(Long sceneId, Long versionId) {
        sceneRepo.findByIdAndOrganisationId(sceneId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("VR Scene not found"));

        VRSceneVersion targetVersion = versionRepo.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("VR Scene Version not found"));

        if (!targetVersion.getScene().getId().equals(sceneId)) {
            throw new IllegalArgumentException("Version does not belong to the specified scene");
        }

        versionRepo.deactivateAllVersionsForScene(sceneId);
        targetVersion.setActive(true);

        return sceneMapper.toVersionResponse(versionRepo.save(targetVersion));
    }

    public Page<VRSceneResponse> getAllScenes(Pageable pageable) {
        return sceneRepo.findAllByOrganisationId(tenantProvider.get(), pageable)
                .map(sceneMapper::toResponse);
    }

}
