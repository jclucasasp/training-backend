package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.quiz.OptionRequest;
import org.lucas.arbackend.dto.quiz.QuestionRequest;
import org.lucas.arbackend.dto.quiz.QuizRequest;
import org.lucas.arbackend.dto.quiz.QuizResponse;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.quiz.QuizQuestion;
import org.lucas.arbackend.entity.quiz.QuizQuestionOption;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.TenantEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
imports = {
        org.lucas.arbackend.entity.course.Course.class
})
public interface QuizMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", source = "courseId")
    Quiz toEntity(QuizRequest request, @Context MappingContext ctx);

    @Mapping(target = "id", ignore = true)
    QuizQuestion toQuestionEntity(QuestionRequest request, @Context MappingContext ctx);

    @Mapping(target = "id", ignore = true)
    QuizQuestionOption toOptionEntity(OptionRequest request, @Context MappingContext ctx);

    // This handles Quiz, QuizQuestion, and QuizQuestionOption automatically
    @AfterMapping
    default void linkTenant(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        if (ctx != null && ctx.getOrganisation() != null) {
            entity.setOrganisation(ctx.getOrganisation());
        }
    }

    default Course mapIdToCourse(Long id) {
        if (id == null) return null;
        Course course = new Course();
        course.setId(id);
        return course;
    }

    QuizResponse toResponse(Quiz quiz);
}
