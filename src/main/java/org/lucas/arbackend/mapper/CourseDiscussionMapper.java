package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.QAndA.CourseQuestionRequest;
import org.lucas.arbackend.dto.QAndA.CourseQuestionResponse;
import org.lucas.arbackend.dto.QAndA.ReplyRequest;
import org.lucas.arbackend.dto.QAndA.ReplyResponse;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.QAndA.CourseQuestion;
import org.lucas.arbackend.entity.QAndA.CourseQuestionReply;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.util.TenantEntity;
import org.mapstruct.*;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
imports = {
        org.lucas.arbackend.entity.course.Course.class,
        org.lucas.arbackend.entity.course.ChapterSection.class
})
public interface CourseDiscussionMapper {
 @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", source = "courseId")
    @Mapping(target = "section", source = "sectionId")
    CourseQuestion toQuestionEntity(CourseQuestionRequest request, @Context MappingContext ctx);

    @Mapping(target = "id", ignore = true)
    CourseQuestionReply toReplyEntity(ReplyRequest request, @Context MappingContext ctx);

    @AfterMapping
    default void linkTenantAndAuthors(@MappingTarget TenantEntity entity, @Context MappingContext ctx) {
        // Handle Organisation via Interface
        entity.setOrganisation(ctx.getOrganisation());

        // Handle specific Author logic
        if (entity instanceof CourseQuestion q) {
            q.setStudent(ctx.getStudent());
        }
        if (entity instanceof CourseQuestionReply r) {
            if (ctx.getStaff() != null) r.setStaff(ctx.getStaff());
            else r.setStudent(ctx.getStudent());
        }
    }

    @Mapping(target = "studentName", expression = "java(q.getStudent().getFirstName() + ' ' + q.getStudent().getLastName())")
    CourseQuestionResponse toQuestionResponse(CourseQuestion q);

    @Mapping(target = "authorName", expression = "java(r.getStaff() != null ? r.getStaff().getFirstName() : r.getStudent().getFirstName())")
    @Mapping(target = "isStaff", expression = "java(r.getStaff() != null)")
    ReplyResponse toReplyResponse(CourseQuestionReply r);

    default Course mapIdToCourse(Long id) {
        if (id == null) return null;
        Course course = new Course();
        course.setId(id);
        return course;
    }

    default ChapterSection mapIdToChapterSection(Long id) {
        if (id == null) return null;
        ChapterSection section = new ChapterSection();
        section.setId(id);
        return section;
    }
}
