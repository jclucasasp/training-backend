package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.course.CourseChapterRequest;
import org.lucas.arbackend.dto.course.CourseRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.dto.course.ChapterSectionRequest;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {


    @Mapping(target = "id", ignore = true)
     void updateCourse(CourseRequest dto, @MappingTarget Course entity);

    // MapStruct will automatically look for this if CourseRequest has a List<CourseChapterRequest>
    @Mapping(target = "id", ignore = true)
    void updateChapter(CourseChapterRequest dto, @MappingTarget Chapter entity);

    @Mapping(target = "id", ignore = true)
    void updateChapterSection(ChapterSectionRequest dto, @MappingTarget ChapterSection entity);

    CourseResponse maptoCourseResponse(Course course);
}
