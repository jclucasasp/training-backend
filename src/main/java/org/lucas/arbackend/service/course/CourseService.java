package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.mapper.CourseMapper;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = findOrganisation();

        Staff staff = staffRepo.findByEmailAndOrganisationIdAndEndedAtIsNull(request.getStaffEmail(), org.getId())
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        // Map DTO to Entity
        Course course = new Course();
//        course.setTotalTimeInMinutes(0);

        courseMapper.updateCourse(request, course);

        course.setOrganisation(org);
        course.setStaff(staff);

        if (request.getChapters() != null) {
        Set<Chapter> chapters = request.getChapters().stream().map(chapterDto -> {
            Chapter chapter = new Chapter();

//            chapter.setTotalTimeInMinutes(0);
            courseMapper.updateChapter(chapterDto, chapter);
            // LINK THE BACK-REFERENCE
            chapter.setCourse(course);

            // 3. MANUALLY MAP AND LINK SECTIONS
            if (chapterDto.getSections() != null) {
                List<ChapterSection> sections = chapterDto.getSections().stream().map(sectionDto -> {
                    ChapterSection section = new ChapterSection();
                    // USE THE MAPPER HERE
                    courseMapper.updateChapterSection(sectionDto, section);

                    // LINK THE BACK-REFERENCE
//                    if (chapter.getTotalTimeInMinutes() != null){
//                        chapter.setTotalTimeInMinutes(chapter.getTotalTimeInMinutes() + sectionDto.getDurationInMinutes());
//                    }

                    section.setChapter(chapter);
                    return section;
                }).toList();

                chapter.setChapterSections(sections);
            }

            return chapter;
        }).collect(Collectors.toSet());

        course.setChapters(chapters);
    }
        Integer totalDuration = getTotalDuration(course);
        course.setTotalTimeInMinutes(totalDuration);

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

        Integer totalDuration = getTotalDuration(course);
        course.setTotalTimeInMinutes(totalDuration);

        Course saved = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(saved);
    }

   private void updateChapters(Course course, List<CourseChapterRequest> chaptersRequest) {

        List<Chapter> chapters = chaptersRequest.stream().map(chapterDto -> {
                        Chapter chapter = (chapterDto.getId() != null)
                                ? course.getChapters().stream()
                                .filter(c -> c.getId().equals(chapterDto.getId())).findFirst()
                                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"))
                                : new Chapter();


                        courseMapper.updateChapter(chapterDto, chapter);
                        chapter.setCourse(course);;
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

    List<ChapterSection> updatedSections = sectionRequest.stream().map(sectionDto -> {
        ChapterSection section = (sectionDto.getId() != null)
                ? chapter.getChapterSections().stream()
                .filter(s -> s.getId().equals(sectionDto.getId())).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Section not found"))
                : new ChapterSection();
        courseMapper.updateChapterSection(sectionDto, section);
        section.setChapter(chapter);

        return section;
    }).toList();

    // Replace the collection to trigger orphanRemoval
    chapter.getChapterSections().clear();
    chapter.getChapterSections().addAll(updatedSections);

    int totalChapterTime = updatedSections.stream()
            .mapToInt(s -> s.getDurationInMinutes() != null ? s.getDurationInMinutes() : 0)
            .sum();
    chapter.setTotalTimeInMinutes(totalChapterTime);
}

    public void softDeleteCourse(Long courseId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
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

    private Integer getTotalDuration(Course course) {
        return course.getChapters().stream()
                .flatMap(chapter -> chapter.getChapterSections().stream())
                .mapToInt(section -> section.getDurationInMinutes() != null ? section.getDurationInMinutes() : 0).sum();
    }
}
