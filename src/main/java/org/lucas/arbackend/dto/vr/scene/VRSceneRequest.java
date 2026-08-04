package org.lucas.arbackend.dto.vr.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
@Data @Builder
@Schema(name = "VRSceneRequest", description = "VR scene request")
public class VRSceneRequest {
    @JsonIgnore
    Long id;

    @NotBlank(message = "Scene title is required")
    @Schema(description = "The title of the scene", example = "Confined Space Entry Simulation")
    private String title;

    @NotBlank(message = "Scene description is required")
    @Schema(description = "The description of the scene", example = "Hazardous environment safety training")
    private String description;

}
