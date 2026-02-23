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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    // TODO : Test if the below methods work
    private final CourseRepository courseRepo;
    private final OrganisationRepository orgRepo;
    private final TenantProvider tenantProvider;
    private final CourseMapper courseMapper;
    private final StaffRepository staffRepo;

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = findOrganisation();

        Staff staff = staffRepo.findByEmailAndEndedAtIsNull(request.getStaffEmail())
                .filter(s -> s.getOrganisation().getId().equals(org.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        // Map DTO to Entity
        Course course = new Course();
        courseMapper.updateCourse(request, course);

        course.setOrganisation(org);
        course.setStaff(staff);

        // Map Modules & Sections
        if(course.getChapters() != null) {
            course.getChapters().forEach(chapter -> {
                chapter.setCourse(course);
                if (chapter.getChapterSections() != null) {
                    chapter.getChapterSections().forEach(ChapterSection ->
                            ChapterSection.setChapter(chapter));

                }
        });
        }

        Course saved = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(saved);
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

        Course saved = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(saved);
    }

   private void updateChapters(Course course, Set<CourseChapterRequest> chaptersRequest) {
    // Use a HashMap for O(1) lookups
    Map<Long, Chapter> existingChaptersMap = course.getChapters().stream()
            .collect(Collectors.toMap(Chapter::getId, Function.identity()));

    Set<Chapter> updatedChapters = new HashSet<>();

    for (CourseChapterRequest req : chaptersRequest) {
        Chapter chapter = (req.getId() != null && existingChaptersMap.containsKey(req.getId()))
                ? existingChaptersMap.get(req.getId()) // Update existing
                : new Chapter(); // Create new

        // Map fields from DTO to Entity
        courseMapper.updateChapter(req, chapter);
        chapter.setCourse(course); // Ensure parent is set

        // Handle Sections recursively
        if (req.getSections() != null) {
            updateChapterSections(chapter, req.getSections());
        }

        updatedChapters.add(chapter);
    }

    // This single line triggers Hibernate to:
    // 1. Insert new chapters
    // 2. Update existing chapters
    // 3. Delete chapters not in 'updatedChapters' (orphanRemoval)
    course.setChapters(updatedChapters);
}

private void updateChapterSections(Chapter chapter, Set<ChapterSectionRequest> sectionRequests) {
    // If the request explicitly provides a null or empty set,
    // we might want to clear existing sections depending on business logic.
    // Assuming null means "no change" and empty means "remove all".
    if (sectionRequests == null) return;

    Map<Long, ChapterSection> existingMap = chapter.getChapterSections().stream()
            .collect(Collectors.toMap(ChapterSection::getId, Function.identity()));

    Set<ChapterSection> updatedSections = new HashSet<>();

    for (ChapterSectionRequest req : sectionRequests) {
        ChapterSection section = (req.getId() != null && existingMap.containsKey(req.getId()))
                ? existingMap.get(req.getId())
                : new ChapterSection();

        courseMapper.updateChapterSection(req, section);
        section.setChapter(chapter);
        updatedSections.add(section);
    }

    // Replace the collection to trigger orphanRemoval
    chapter.setChapterSections(updatedSections);
}

    private void softDeleteCourse(Long courseId, Long orgId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
    }

    // Helper mapper
//    private CourseResponse mapToResponse(Course course) {
//        return CourseResponse.builder()
//                .id(course.getId())
//                .imageUrl(course.getImageUrl())
//                .name(course.getName())
//                .description(course.getDescription())
//                .difficulty(course.getDifficultyTypes().name())
//                .imageUrl(course.getImageUrl())
//                .chapterResponses(course.getChapters().stream().map(m -> CourseChapterResponse.builder()
//                        .id(m.getId())
//                        .description(m.getDescription())
//                        .sections(m.getChapterSections().stream().map(s -> ChapterSectionResponse.builder()
//                                .id(s.getId())
//                                .title(s.getTitle())
//                                .duration(s.getDuration())
//                                .resourceUrl(s.getResourceUrl())
//                                .resourceMediaType(s.getResourceMediaType())
//                                .build()).collect(Collectors.toSet()))
//                        .build()).collect(Collectors.toSet()))
//                .tags(course.getTags())
//                .build();
//    }

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
}
