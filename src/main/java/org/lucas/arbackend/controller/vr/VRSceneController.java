package org.lucas.arbackend.controller.vr;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.scene.VRSceneResolutionResponse;
import org.lucas.arbackend.service.vr.VRSceneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/vr/scene")
@RequiredArgsConstructor
@Tag(name = "10. VR Scene", description = "VR Scene API")
public class VRSceneController {
    private final VRSceneService sceneService;

    @GetMapping("/active-resolution")
    public ResponseEntity<VRSceneResolutionResponse> resolveActiveSceneForSection(Long sectionId) {
        return ResponseEntity.ok(sceneService.resolveActiveSceneForSection(sectionId));
    }

}
