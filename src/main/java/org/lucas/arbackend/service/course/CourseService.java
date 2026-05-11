package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.misc.Attachment;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.mapper.CourseMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.course.ChapterRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.quiz.QuizRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.service.quiz.QuizService;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepo;
    private final OrganisationRepository orgRepo;
    private final TenantProvider tenantProvider;
    private final CourseMapper courseMapper;
    private final StaffRepository staffRepo;
    private final QuizRepository quizRepo;
    private final QuizService quizService;
    private final ChapterRepository chapterRepo;

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = findOrganisation();

        Staff staff = staffRepo.findByEmailAndOrganisationId(request.getStaffEmail(), org.getId())
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        MappingContext ctx = new MappingContext(org, null, staff);
        // Map DTO to Entity
        Course course = new Course();
        course.setOrganisation(org);
        course.setStaff(staff);
        courseMapper.updateCourse(request, course, ctx);

        if (request.getChapters() != null) {
            AtomicInteger chapterIndex = new AtomicInteger(0);
            Set<Chapter> chapters = request.getChapters().stream().map(chapterDto -> {
                Chapter chapter = new Chapter();

                courseMapper.updateChapter(chapterDto, chapter, ctx);
                // LINK THE BACK-REFERENCE
                chapter.setCourse(course);
                chapter.setOrderIndex(chapterIndex.getAndIncrement());

                // 3. MANUALLY MAP AND LINK SECTIONS
                if (chapterDto.getSections() != null) {
                    AtomicInteger sectionIndex = new AtomicInteger(0);
                    List<ChapterSection> sections = chapterDto.getSections().stream().map(sectionDto -> {
                        ChapterSection section = new ChapterSection();

                        courseMapper.updateChapterSection(sectionDto, section, ctx);
                        section.setChapter(chapter);
                        section.setOrderIndex(sectionIndex.getAndIncrement());

                        return section;
                    }).toList();

                    // Calculate the total minutes for a chapter
                    Integer totalChapterMinutes = getTotalDuration(sections);
                    course.setTotalTimeInMinutes(totalChapterMinutes);

                    chapter.setChapterSections(sections);
                }

                chapter.setTotalTimeInMinutes(getTotalDuration(chapter.getChapterSections()));
                return chapter;
            }).collect(Collectors.toSet());

            course.setChapters(chapters);
    }
        // Calculate the total minutes for the course
        course.setTotalTimeInMinutes(getTotalDuration(course.getChapters()));

        return courseMapper.maptoCourseResponse(courseRepo.save(course));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getPaginatedCourses(Pageable pageable) {

        Long orgId = tenantProvider.get();

        return courseRepo.findAllByOrganisationId(orgId, pageable)
                .map(courseMapper::maptoCourseResponse);
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Organisation org = findOrganisation();
        Course course = courseRepo.findByIdAndOrganisationId(courseId, org.getId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));

        MappingContext ctx = new MappingContext(org, null, course.getStaff());
        // 1. Update simple Course fields
        courseMapper.updateCourse(request, course, ctx);

        // 2. Update Modules and Sections if provided
        if (request.getChapters() != null) {
            updateChapters(course, request.getChapters(), ctx);
        }

        course.setTotalTimeInMinutes(getTotalDuration(course.getChapters()));

        return courseMapper.maptoCourseResponse(courseRepo.save(course));
    }

    private void updateChapters(Course course, List<CourseChapterRequest> chaptersRequest, MappingContext ctx) {
        // 1. Double-check: Is the Quiz ID really the Course ID?
        // If not, change this to findByCourseId...
        Optional<Quiz> courseQuiz = quizRepo.findByCourseIdAndOrganisationId(course.getId(), tenantProvider.get());

        if (courseQuiz.isPresent()) {
            log.info("Found Quiz ID: {} for Course ID: {}", courseQuiz.get().getId(), course.getId());
            // Clear the QUIZ side of the old links so the session is clean
            courseQuiz.get().getChapterQuizzes().clear();
        } else {
            log.error("No Quiz found for Course {}. Links will NOT be created.", course.getId());
        }

        course.getChapters().clear();
        chapterRepo.flush();
        quizRepo.flush();

        AtomicInteger index = new AtomicInteger();
        for (CourseChapterRequest dto : chaptersRequest) {
            Chapter chapter = new Chapter();
            chapter.setCourse(course);
            chapter.setOrganisation(ctx.getOrganisation());
            courseMapper.updateChapter(dto, chapter, ctx);
            chapter.setOrderIndex(index.getAndIncrement());

            course.getChapters().add(chapter);

            // Save the chapter first so it has an ID
            Chapter savedChapter = chapterRepo.saveAndFlush(chapter);

            courseQuiz.ifPresent(quiz -> {
                log.info("Attempting to link Quiz {} to New Chapter {}", quiz.getId(), savedChapter.getId());
                quizService.assignQuizToChapter(quiz, savedChapter);
            });

            updateChapterSections(savedChapter, dto.getSections(), ctx);
        }
    }

private void updateChapterSections(Chapter chapter, List<ChapterSectionRequest> sectionRequest, MappingContext ctx) {
    // If the request explicitly provides a null or empty list,
    // we might want to clear existing sections depending on business logic.
    // Assuming null means "no change" and empty means "remove all".
    if (sectionRequest == null)  return;
    log.info("DEBUG: Incoming update for chapter sections for chapter: [{}]", chapter.getId());

    AtomicInteger index = new AtomicInteger();
    List<ChapterSection> updatedSections = sectionRequest.stream().map(sectionDto -> {
                log.info("DEBUG: Adding section [{}]", sectionDto.getId());
        ChapterSection section = (sectionDto.getId() != null)
                ? chapter.getChapterSections().stream()
                .filter(s -> s.getId().equals(sectionDto.getId())).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Section not found"))
                : new ChapterSection();
        courseMapper.updateChapterSection(sectionDto, section, ctx);
        section.setChapter(chapter);
        section.setOrderIndex(index.getAndIncrement());

        if (sectionDto.getAttachments() != null) {
            log.info("DEBUG: Adding attachments to section: [{}]", section.getId());
            List<Attachment> attachments = sectionDto.getAttachments().stream().map(attDto -> {
                Attachment attachment = (attDto.getId() != null && sectionDto.getAttachments() != null)
                        ? section.getAttachments().stream()
                        .filter(a -> a.getId().equals(attDto.getId())).findFirst()
                        .orElse(new Attachment())
                        : new Attachment();
                courseMapper.updateAttachment(attDto, attachment, ctx);
                attachment.setChapterSection(section);
                return attachment;
            }).toList();

            section.getAttachments().clear();
            section.getAttachments().addAll(attachments);
        }

        return section;
    }).toList();

    // Replace the collection to trigger orphanRemoval
    chapter.getChapterSections().clear();
    chapter.getChapterSections().addAll(updatedSections);
    chapter.setTotalTimeInMinutes(getTotalDuration(chapter.getChapterSections()));
}

    public void softDeleteCourse(Long courseId) {

        Course course = courseRepo.findByIdAndOrganisationId(courseId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
    }

    public void removeQuizFromChapter(Long chapterId, Long quizId) {
    Chapter chapter = chapterRepo.findByIdAndOrganisationId(chapterId, tenantProvider.get())
            .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

    // Remove by ID from the Set
    chapter.getChapterQuizzes().removeIf(q -> q.getId().equals(quizId));
    chapterRepo.save(chapter);
}

    @Transactional(readOnly = true)
    private Organisation findOrganisation() {
        Long orgId = tenantProvider.get();

        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No organisation found for tenant id: [" + orgId +"]"));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long courseId, Long staffId) {
        return courseRepo.findById(courseId)
                .map(course -> course.getStaff().getId().equals(staffId))
                .orElse(false);
    }

    private Integer getTotalDuration(Set<Chapter> chapters) {
        return chapters.stream()
                .flatMap(chapter -> chapter.getChapterSections().stream())
                .mapToInt(section -> section.getDurationInMinutes() != null ? section.getDurationInMinutes() : 0).sum();
    }

    private Integer getTotalDuration(List<ChapterSection> sections) {
        return sections.stream()
                .mapToInt(section -> section.getDurationInMinutes() != null ? section.getDurationInMinutes() : 0).sum();
    }
}
