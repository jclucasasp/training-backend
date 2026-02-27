package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.course.*;
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
    @Mapping(target = "chapterSections", ignore = true)
    @Mapping(target = "totalTimeInMinutes", ignore = true)
    void updateChapter(CourseChapterRequest dto, @MappingTarget Chapter entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "durationInMinutes", source = "durationInMinutes")
    void updateChapterSection(ChapterSectionRequest dto, @MappingTarget ChapterSection entity);

    @Mapping(target = "staffEmail", source = "staff.email")
    @Mapping(target = "difficulty", source = "course.difficultyTypes")
    @Mapping(target = "chaptersResponse", source = "chapters")
    @Mapping(target = "totalTimeInMinutes", source = "totalTimeInMinutes")
    CourseResponse maptoCourseResponse(Course course);

    @Mapping(target = "sectionsResponse", source = "chapterSections") // Map Set<ChapterSection> to Set<ChapterSectionResponse>
    CourseChapterResponse mapToChapterResponse(Chapter chapter);

    // 3. Define how ONE Section maps to ONE SectionResponse
    // (MapStruct handles this automatically if field names match, but you can be explicit)
    ChapterSectionResponse mapToSectionResponse(ChapterSection section);
}
