package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.course.CourseRequest;
import org.lucas.arbackend.dto.course.ModuleRequest;
import org.lucas.arbackend.dto.course.SectionRequest;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.Section;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

     void updateCourse(CourseRequest dto, @MappingTarget Course entity);

    // MapStruct will automatically look for this if CourseRequest has a List<ModuleRequest>
    void updateModule(ModuleRequest dto, @MappingTarget Module entity);

    void updateSection(SectionRequest dto, @MappingTarget Section entity);
}
