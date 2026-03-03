package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.entity.student.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "studentQuizzes", source = "studentQuizzes")
    StudentResponse maptToStudentResponse(Student entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentQuizzes", ignore = true)
    void updateStudent(StudentRequest dto, @MappingTarget Student entity);
}
