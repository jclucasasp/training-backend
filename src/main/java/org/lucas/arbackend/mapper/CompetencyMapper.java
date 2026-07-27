package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.vr.competency.*;
import org.lucas.arbackend.entity.vr.competency.Competency;
import org.lucas.arbackend.entity.vr.competency.CompetencyAssessment;
import org.lucas.arbackend.entity.vr.competency.CompetencyCriterion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompetencyMapper {
    Competency toCompetencyEntity(CompetencyCreateRequest dto);
    CompetencyResponse toCompetencyResponse(Competency entity);
    void updateCompetencyFromRequest(CompetencyCreateRequest dto, @MappingTarget Competency entity);

    CompetencyCriterion toCriterionEntity(CompetencyCriterionRequest dto);
    CompetencyCriterionResponse toCriterionResponse(CompetencyCriterion entity);

    @Mapping(target = "session", ignore = true)
    @Mapping(target = "competency", ignore = true)
    CompetencyAssessment toAssessmentEntity(CompetencyAssessmentCreateRequest dto);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "competencyId", source = "competency.id")
    @Mapping(target = "studentNumber", source = "student.studentNumber")
    CompetencyAssessmentResponse toAssessmentResponse(CompetencyAssessment entity);
}
