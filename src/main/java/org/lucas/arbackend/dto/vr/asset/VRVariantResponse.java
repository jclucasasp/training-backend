package org.lucas.arbackend.dto.vr.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.lucas.arbackend.entity.vr.asset.VRPlatformType;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Schema(name = "VRVariantResponse", description = "VR asset variant response")
public record VRVariantResponse(
        Long id,
        @Schema(description = "Type of platform currently used", example = "PC VR")
        VRPlatformType platformType,
        @Schema(description = "Level of detail for the asset variant", example = "1")
        Integer lodLevel,
        @Schema(description = "URL to the asset variant file", example = "https://example.com/asset/variant/file")
        String url,
        @Schema(description = "Size of the asset variant file in bytes", example = "12345678")
        Integer fileSize,
        @Schema(description = "Checksum of the asset variant file", example = "12345678")
        String checksum
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
