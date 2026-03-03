package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.misc.Quiz;
import org.lucas.arbackend.mapper.CourseMapper;
import org.lucas.arbackend.repository.course.ChapterRepository;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.course.QuizRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private final ChapterRepository chapterRepo;

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = findOrganisation();

        Staff staff = staffRepo.findByEmailAndOrganisationIdAndEndedAtIsNull(request.getStaffEmail(), org.getId())
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        // Map DTO to Entity
        Course course = new Course();
        courseMapper.updateCourse(request, course);
        course.setOrganisation(org);
        course.setStaff(staff);

        if (request.getChapters() != null) {
            AtomicInteger chapterIndex = new AtomicInteger(0);

            Set<Chapter> chapters = request.getChapters().stream().map(chapterDto -> {
                Chapter chapter = new Chapter();

                courseMapper.updateChapter(chapterDto, chapter);
                // LINK THE BACK-REFERENCE
                chapter.setCourse(course);
                chapter.setOrderIndex(chapterIndex.getAndIncrement());

                // 3. MANUALLY MAP AND LINK SECTIONS
                if (chapterDto.getSections() != null) {
                    AtomicInteger sectionIndex = new AtomicInteger(0);

                    List<ChapterSection> sections = chapterDto.getSections().stream().map(sectionDto -> {
                        ChapterSection section = new ChapterSection();
                        // USE THE MAPPER HERE
                        courseMapper.updateChapterSection(sectionDto, section);

                        section.setChapter(chapter);
                        section.setOrderIndex(sectionIndex.getAndIncrement());

                        return section;
                    }).toList();

                    // Calculate the total minutes for a chapter
                    Integer totalChapterMinutes = getTotalDuration(sections);
                    course.setTotalTimeInMinutes(totalChapterMinutes);

                    chapter.setChapterSections(sections);
                }

                return chapter;
            }).collect(Collectors.toSet());

            course.setChapters(chapters);
    }
        // Calculate the total minutes for the course
        Integer totalCourseMinutes = getTotalDuration(course.getChapters());
        course.setTotalTimeInMinutes(totalCourseMinutes);

        Course newCourse = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(newCourse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getPaginatedCourses(Pageable pageable) {

        Long orgId = tenantProvider.get();

        return courseRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(courseMapper::maptoCourseResponse);
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Long orgId = tenantProvider.get();

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));

        // 1. Update simple Course fields
        courseMapper.updateCourse(request, course);

        // 2. Update Modules and Sections if provided
        if (request.getChapters() != null) {
            updateChapters(course, request.getChapters());
        }

        Integer totalCourseMinutes = getTotalDuration(course.getChapters());
        course.setTotalTimeInMinutes(totalCourseMinutes);

        Course saved = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(saved);
    }

   private void updateChapters(Course course, List<CourseChapterRequest> chaptersRequest) {

        AtomicInteger index = new AtomicInteger();
        List<Chapter> chapters = chaptersRequest.stream().map(chapterDto -> {
                        Chapter chapter = (chapterDto.getId() != null)
                                ? course.getChapters().stream()
                                .filter(c -> c.getId().equals(chapterDto.getId())).findFirst()
                                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"))
                                : new Chapter();

                        courseMapper.updateChapter(chapterDto, chapter);
                        chapter.setCourse(course);
                        chapter.setOrderIndex(index.getAndIncrement());

                        updateChapterSections(chapter, chapterDto.getSections());

                        return chapter;
        }).toList();

        course.getChapters().clear();
        course.getChapters().addAll(chapters);
   }

private void updateChapterSections(Chapter chapter, List<ChapterSectionRequest> sectionRequest) {
    // If the request explicitly provides a null or empty list,
    // we might want to clear existing sections depending on business logic.
    // Assuming null means "no change" and empty means "remove all".
    if (sectionRequest == null)  return;

    AtomicInteger index = new AtomicInteger();
    List<ChapterSection> updatedSections = sectionRequest.stream().map(sectionDto -> {
        ChapterSection section = (sectionDto.getId() != null)
                ? chapter.getChapterSections().stream()
                .filter(s -> s.getId().equals(sectionDto.getId())).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Section not found"))
                : new ChapterSection();
        courseMapper.updateChapterSection(sectionDto, section);
        section.setChapter(chapter);
        // This sets the index order
        section.setOrderIndex(index.getAndIncrement());

        return section;
    }).toList();

    // Replace the collection to trigger orphanRemoval
    chapter.getChapterSections().clear();
    chapter.getChapterSections().addAll(updatedSections);

    int totalChapterTime = getTotalDuration(chapter.getChapterSections());
    chapter.setTotalTimeInMinutes(totalChapterTime);
}

    public void softDeleteCourse(Long courseId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
    }

    public void addQuizToChapter(Long chapterId, Long quizId) {
        Chapter chapter = chapterRepo.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

        Quiz quiz = quizRepo.findByIdAndOrganisationIdAndEndedAtIsNull(quizId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

        chapter.getQuizzes().add(quiz);
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
