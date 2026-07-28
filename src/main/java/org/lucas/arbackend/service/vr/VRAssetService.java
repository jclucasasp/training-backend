package org.lucas.arbackend.service.vr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.asset.VRAssetCreateRequest;
import org.lucas.arbackend.dto.vr.asset.VRAssetResponse;
import org.lucas.arbackend.dto.vr.asset.VRVariantResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.vr.asset.VRAsset;
import org.lucas.arbackend.entity.vr.asset.VRAssetVariant;
import org.lucas.arbackend.repository.vr.AssetRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VRAssetService {
    private final TenantProvider tenantProvider;
    private final AssetRepository assetRepo;

    public VRAssetResponse registerAsset(VRAssetCreateRequest request) {
        Organisation org = tenantProvider.getOrg();

        VRAsset asset = VRAsset.builder()
                .organisation(org)
                .name(request.getName())
                .assetType(request.getAssetType())
                .build();

        if (request.getVariants() != null) {
            asset.setVariants(request.getVariants().stream()
                    .map(v ->
                                    VRAssetVariant.builder()
                                            .asset(asset)
                                            .platformType(v.platformType())
                                            .lodLevel(v.lodLevel())
                                            .url(v.url())
                                            .fileSize(v.fileSize())
                                            .checksum(v.checksum())
                                            .build()
                    ).toList());
        }

        VRAsset savedAsset = assetRepo.save(asset);
        return mapToResponse(savedAsset);
    }

    public VRAssetResponse getAsset(Long assetId) {
        VRAsset asset = assetRepo.findByIdAndOrganisationId(assetId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        return mapToResponse(asset);
    }

    private VRAssetResponse mapToResponse(VRAsset asset) {
        return VRAssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .assetType(asset.getAssetType())
                .variants(asset.getVariants().stream()
                        .map(v -> VRVariantResponse.builder()
                                .id(v.getId())
                                .platformType(v.getPlatformType())
                                .lodLevel(v.getLodLevel())
                                .url(v.getUrl())
                                .fileSize(v.getFileSize())
                                .checksum(v.getChecksum())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
