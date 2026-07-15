package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.vr.VREventResponse;
import org.lucas.arbackend.dto.vr.VRSessionResponse;
import org.lucas.arbackend.entity.vr.event.VREvent;
import org.lucas.arbackend.entity.vr.VRSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VRSessionMapper {

    @Mapping(target = "studentNumber", source = "student.studentNumber")
    @Mapping(target = "studentName", expression = "java(session.getStudent().getFirstName() + ' ' + session.getStudent().getLastName())")
    @Mapping(target = "sectionId", source = "chapterSection.id")
    @Mapping(target = "sectionTitle", source = "chapterSection.title")
    VRSessionResponse toResponse(VRSession session);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "hand", source = "hand")
    VREventResponse toEventResponse(VREvent event);
}
