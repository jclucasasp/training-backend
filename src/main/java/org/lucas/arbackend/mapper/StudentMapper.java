package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.student.StudentQuizResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "studentQuizzes", source = "studentQuizzes")
    @Mapping(target = "organisation", source = "entity.organisation.id")
    StudentResponse mapToStudentResponse(Student entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentQuizzes", ignore = true)
    @Mapping(target = "organisation", ignore = true)
    void updateStudent(StudentRequest dto, @MappingTarget Student entity, @Context MappingContext ctx);

    @Mapping(target = "quizId", source = "quiz.id")
    StudentQuizResponse mapToStudentQuizResponse(StudentQuiz entity);

    @AfterMapping
    default void linkTenant(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        if (ctx != null && ctx.getOrganisation() != null) {
            entity.setOrganisation(ctx.getOrganisation());
        }
    }
}
