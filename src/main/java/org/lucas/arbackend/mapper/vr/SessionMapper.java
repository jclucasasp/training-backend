package org.lucas.arbackend.mapper.vr;

import org.lucas.arbackend.dto.vr.event.VREventResponse;
import org.lucas.arbackend.dto.vr.session.VRSessionResponse;
import org.lucas.arbackend.entity.vr.event.VREvent;
import org.lucas.arbackend.entity.vr.VRSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "studentNumber", source = "student.studentNumber")
    @Mapping(target = "studentName", expression = "java(session.getStudent().getFirstName() + ' ' + session.getStudent().getLastName())")
    @Mapping(target = "sectionId", source = "chapterSection.id")
    @Mapping(target = "sectionTitle", source = "chapterSection.title")
    @Mapping(target = "sceneVersionId", source = "sceneVersion.id")
    VRSessionResponse toResponse(VRSession session);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "hand", source = "hand")
    VREventResponse toEventResponse(VREvent event);
}
