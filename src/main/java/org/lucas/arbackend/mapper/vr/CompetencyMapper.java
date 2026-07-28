package org.lucas.arbackend.mapper.vr;

import org.lucas.arbackend.dto.vr.competency.*;
import org.lucas.arbackend.entity.vr.competency.Competency;
import org.lucas.arbackend.entity.vr.competency.CompetencyAssessment;
import org.lucas.arbackend.entity.vr.competency.CompetencyCriterion;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompetencyMapper {
    CompetencyResponse toCompetencyResponse(Competency entity);
    Competency updateCompetency(CompetencyCreateRequest dto, @MappingTarget Competency entity, @Context MappingContext ctx);

    CompetencyCriterion toCriterionEntity(CompetencyCriterionRequest dto);
    CompetencyCriterionResponse toCriterionResponse(CompetencyCriterion entity);
    void updateCriterion(CompetencyCriterionRequest dto, @MappingTarget CompetencyCriterion entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "competency",ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateAssessment(CompetencyAssessmentCreateRequest dto, @MappingTarget CompetencyAssessment entity);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "competencyId", source = "competency.id")
    @Mapping(target = "studentNumber", source = "student.studentNumber")
    CompetencyAssessmentResponse toAssessmentResponse(CompetencyAssessment entity);

    @AfterMapping
    default void linkTenant(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        if (ctx != null && ctx.getOrganisation() != null ) {
            entity.setOrganisation(ctx.getOrganisation());
        }
    }
}
