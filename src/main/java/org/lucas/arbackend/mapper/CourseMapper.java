package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.dto.course.attachment.AttachmentRequest;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.misc.Attachment;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", source = "status")
//    @Mapping(target = "organisation", ignore = true)
     void updateCourse(CourseRequest dto, @MappingTarget Course entity, @Context MappingContext ctx);

    // MapStruct will automatically look for this if CourseRequest has a List<CourseChapterRequest>
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapterSections", ignore = true)
    @Mapping(target = "status", source = "status")
//    @Mapping(target = "organisation", ignore = true)
    void updateChapter(CourseChapterRequest dto, @MappingTarget Chapter entity, @Context MappingContext ctx);

    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "isPreview", source = "isPreview")
//    @Mapping(target = "status", source = "status")
    @Mapping(target = "durationInMinutes", source = "durationInMinutes")
//    @Mapping(target = "organisation", ignore = true)
    void updateChapterSection(ChapterSectionRequest dto, @MappingTarget ChapterSection entity, @Context MappingContext ctx);

    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "organisation", ignore = true)
    @Mapping(target = "chapterSection", ignore = true)
    void updateAttachment(AttachmentRequest dto, @MappingTarget Attachment entity, @Context MappingContext ctx);

    @Mapping(target = "staffEmail", source = "staff.email")
    @Mapping(target = "difficulty", source = "course.difficultyTypes")
//    @Mapping(target = "status", source = "course.statusTypes")
    @Mapping(target = "chaptersResponse", source = "chapters")
    @Mapping(target = "totalTimeInMinutes", source = "totalTimeInMinutes")
    CourseResponse maptoCourseResponse(Course course);

    @Mapping(target = "sectionsResponse", source = "chapterSections") // Map Set<ChapterSection> to Set<ChapterSectionResponse>
    CourseChapterResponse mapToChapterResponse(Chapter chapter);

    // 3. Define how ONE Section maps to ONE SectionResponse
    // (MapStruct handles this automatically if field names match, but you can be explicit)
    @Mapping(target = "content", source = "content")
    ChapterSectionResponse mapToSectionResponse(ChapterSection section);

    @AfterMapping
    default void linkTenant(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        if (ctx != null && ctx.getOrganisation() != null) {
            entity.setOrganisation(ctx.getOrganisation());
        }
    }

}
