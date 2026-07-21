package org.lucas.arbackend.mapper.context;

import org.lucas.arbackend.dto.vr.scene.VRSceneResponse;
import org.lucas.arbackend.dto.vr.scene.VRSceneVersionResponse;
import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VRSceneMapper {
    VRSceneResponse toResponse(VRScene scene);

    @Mapping(source = "scene.id", target = "sceneId")
    VRSceneVersionResponse toVersionResponse(VRSceneVersion version);
}
