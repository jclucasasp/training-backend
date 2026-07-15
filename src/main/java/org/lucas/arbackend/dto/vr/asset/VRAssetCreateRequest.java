package org.lucas.arbackend.dto.vr.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.entity.vr.asset.VRAssetType;

import java.util.List;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
@Schema(name = "VRAssetCreateRequest", description = "VR asset create request")
public class VRAssetCreateRequest {
    @NotBlank(message = "Asset name is required")
    @Schema(description = "The name of the asses", example = "Heavy Machinery Generator")
    private String name;

    @NotNull(message = "Asset type is required")
    @Schema(description = "The type of asset", example = "MESH")
    private VRAssetType assetType;

    @NotNull(message = "A list of asset variants is required")
    @Schema(description = "The list of asset variants", example = """
            "variants": [
                        { "platform": "PC_VR", "fileSize": 18239044 },
                        { "platform": "STANDALONE_HEADSET", "fileSize": 4194304 }
                    ]
  """)
    private List<VRVariantResponse> variants;
}
