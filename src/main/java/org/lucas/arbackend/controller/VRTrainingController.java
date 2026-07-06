package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.service.vr.VRSessionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/vr")
@RequiredArgsConstructor
@Tag(name = "9. VR Training", description = "VR session telemetry and replay data")
public class VRTrainingController {
    private final VRSessionService sessionService;


}
