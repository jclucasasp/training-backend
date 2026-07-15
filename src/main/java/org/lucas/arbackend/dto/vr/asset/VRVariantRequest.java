package org.lucas.arbackend.dto.vr.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.entity.vr.asset.VRPlatformType;

@AllArgsConstructor @NoArgsConstructor
@Data @Builder
@Schema(name = "VRVariantRequest", description = "VR asset variant request")
public class VRVariantRequest {
    @NotNull(message = "Type of platform is required")
    @Schema(description = "Type of platform being used", example = "PC VR")
    private VRPlatformType platformType;

    @NotNull(message = "Level of detail is required")
    @Schema(description = "Level of detail for the asset variant", example = "1")
    private Integer lodLevel;

    @NotBlank(message = "Asset url is required")
    @Schema(description = "URL to the asset variant file", example = "https://example.com/asset/variant/file")
    private String url;

    @NotNull(message = "File size is required")
    @Schema(description = "Size of the asset variant file in bytes", example = "12345678")
    private Integer fileSize;

    @NotBlank(message = "Checksum is required")
    @Schema(description = "Checksum of the asset variant file", example = "12345678")
    private String checksum;
}
