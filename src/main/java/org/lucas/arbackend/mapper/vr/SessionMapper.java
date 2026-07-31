package org.lucas.arbackend.mapper.vr;

import org.lucas.arbackend.dto.vr.event.VREventRequest;
import org.lucas.arbackend.dto.vr.event.VREventResponse;
import org.lucas.arbackend.dto.vr.session.VRSessionResponse;
import org.lucas.arbackend.entity.vr.event.VREvent;
import org.lucas.arbackend.entity.vr.VRSession;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.mapstruct.*;

import java.util.List;

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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", source = "session")
    @Mapping(target = "organisation", ignore = true)
    @Mapping(target = "durationInMilliseconds", source = "request.durationMs")
    VREvent toEventEntity(VREventRequest request, VRSession session, @Context MappingContext ctx);

    default List<VREvent> toEventEntityList(List<VREventRequest> requestList, VRSession session, @Context MappingContext ctx) {
        if (requestList == null) {
            return List.of();
        }
        return requestList.stream()
                .map(r -> toEventEntity(r, session, ctx))
                .toList();
    }

    @AfterMapping
    default void linkToTenant(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        if (ctx != null && ctx.getOrganisation() != null ) {
            entity.setOrganisation(ctx.getOrganisation());
        }
    }
}
