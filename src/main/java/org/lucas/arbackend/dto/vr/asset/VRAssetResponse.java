package org.lucas.arbackend.dto.vr.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.entity.vr.asset.VRAssetType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
@Schema(name = "VRAssetResponse", description = "VR asset response")
public record VRAssetResponse(
        Long id,
        @Schema(description = "Asset Name", example = "Heavy Machinery Generator")
        String name,
        @Schema(description = "Asset Type", example = "MESH")
        VRAssetType assetType,
        @Schema(description = "List of asset variants")
        List<VRVariantResponse> variants
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
