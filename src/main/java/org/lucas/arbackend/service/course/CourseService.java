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

    // TODO : Test if the below methods work
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
        courseMapper.updateCourse(request, course);

        course.setOrganisation(org);
        course.setStaff(staff);

        if (request.getChapters() != null) {
        Set<Chapter> chapters = request.getChapters().stream().map(chapterDto -> {
            Chapter chapter = new Chapter();
            // USE THE MAPPER HERE
            courseMapper.updateChapter(chapterDto, chapter);

            // LINK THE BACK-REFERENCE (Fixes the null column error)
            chapter.setCourse(course);

            // 3. MANUALLY MAP AND LINK SECTIONS
            if (chapterDto.getSections() != null) {
                Set<ChapterSection> sections = chapterDto.getSections().stream().map(sectionDto -> {
                    ChapterSection section = new ChapterSection();
                    // USE THE MAPPER HERE
                    courseMapper.updateChapterSection(sectionDto, section);

                    // LINK THE BACK-REFERENCE
                    section.setChapter(chapter);
                    return section;
                }).collect(Collectors.toSet());

                chapter.setChapterSections(sections);
            }
            return chapter;
        }).collect(Collectors.toSet());

        course.setChapters(chapters);
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

//    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
//        Long orgId = tenantProvider.get();
//
//        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
//                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));
//
//        // 1. Update simple Course fields
//        courseMapper.updateCourse(request, course);
//
//        // 2. Update Modules and Sections if provided
//        if (request.getChapters() != null) {
//            updateChapters(course, request.getChapters());
//        }
//
//        Course saved = courseRepo.save(course);
//        return courseMapper.maptoCourseResponse(saved);
//    }

//   private void updateChapters(Course course, Set<CourseChapterRequest> chaptersRequest) {
//    // Use a HashMap for O(1) lookups
//    Map<Long, Chapter> existingChaptersMap = course.getChapters().stream()
//            .collect(Collectors.toMap(Chapter::getId, Function.identity()));
//
//    Set<Chapter> updatedChapters = new HashSet<>();
//
//    for (CourseChapterRequest req : chaptersRequest) {
//        Chapter chapter = (req.getId() != null && existingChaptersMap.containsKey(req.getId()))
//                ? existingChaptersMap.get(req.getId()) // Update existing
//                : new Chapter(); // Create new
//
//        // Map fields from DTO to Entity
//        courseMapper.updateChapter(req, chapter);
//        chapter.setCourse(course); // Ensure parent is set
//
//        // Handle Sections recursively
//        if (req.getSections() != null) {
//            updateChapterSections(chapter, req.getSections());
//        }
//
//        updatedChapters.add(chapter);
//    }
//
//    // This single line triggers Hibernate to:
//    // 1. Insert new chapters
//    // 2. Update existing chapters
//    // 3. Delete chapters not in 'updatedChapters' (orphanRemoval)
//    course.setChapters(updatedChapters);
//}
//
//private void updateChapterSections(Chapter chapter, Set<ChapterSectionRequest> sectionRequests) {
//    // If the request explicitly provides a null or empty set,
//    // we might want to clear existing sections depending on business logic.
//    // Assuming null means "no change" and empty means "remove all".
//    if (sectionRequests == null) return;
//
//    Map<Long, ChapterSection> existingMap = chapter.getChapterSections().stream()
//            .collect(Collectors.toMap(ChapterSection::getId, Function.identity()));
//
//    Set<ChapterSection> updatedSections = new HashSet<>();
//
//    for (ChapterSectionRequest req : sectionRequests) {
//        ChapterSection section = (req.getId() != null && existingMap.containsKey(req.getId()))
//                ? existingMap.get(req.getId())
//                : new ChapterSection();
//
//        courseMapper.updateChapterSection(req, section);
//        section.setChapter(chapter);
//        updatedSections.add(section);
//    }
//
//    // Replace the collection to trigger orphanRemoval
//    chapter.setChapterSections(updatedSections);
//}

    private void softDeleteCourse(Long courseId, Long orgId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
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
}
