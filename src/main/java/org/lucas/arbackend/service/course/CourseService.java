package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.mapper.CourseMapper;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
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

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = findOrganisation();

        // Map DTO to Entity
        Course course = new Course();
        courseMapper.updateCourse(request, course);
//        course.setName(request.getName());
//        course.setDescription(request.getDescription());
//        course.setDifficultyTypes(DifficultyTypes.valueOf(request.getDifficultyTypes()));
//        course.setTags(request.getTags());
//        course.setImageUrl(request.getImageUrl());
        course.setOrganisation(org);

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
//        if (request.getCourseModules() != null) {
//            Set<CourseModule> courseModules = request.getCourseModules().stream().map(mReq -> {
//                CourseModule courseModule = new CourseModule();
//                courseModule.setName(mReq.getName());
//                courseModule.setCourse(course); // Link back to parent
//
//                if (mReq.getChapterSections() != null) {
//                    Set<ChapterSectionRequest> chapterSections = mReq.getChapterSections().stream().map(sReq -> {
//                        ChapterSectionRequest chapterSection = new ChapterSectionRequest();
//                        chapterSection.setTitle(sReq.getTitle());
//                        chapterSection.setContent(sReq.getContent());
//                        chapterSection.setResourceUrl(sReq.getResourceUrl());
//                        chapterSection.setResourceMediaType(sReq.getResourceMediaType());
//                        chapterSection.setOrderIndex(sReq.getOrderIndex());
//                        chapterSection.setTags(sReq.getTags());
//                        chapterSection.setCourseModule(courseModule); // Link back to parent
//                        return chapterSection;
//                    }).collect(Collectors.toSet());
//                    courseModule.setChapterSections(chapterSections);
//                }
//                return courseModule;
//            }).collect(Collectors.toSet());
//            course.setCourseModules(courseModules);
//        }

        Course saved = courseRepo.save(course);
        return mapToResponse(saved);
    }

    public Page<CourseResponse> getPaginatedCourses(Pageable pageable) {

        Long orgId = tenantProvider.get();

        return courseRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(this::mapToResponse);
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Long orgId = tenantProvider.get();

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));

        // 1. Update simple Course fields
        courseMapper.updateCourse(request, course);
//        if (request.getName() != null) course.setName(request.getName());
//        if (request.getDescription() != null) course.setDescription(request.getDescription());
//        if (request.getDifficultyTypes() != null) {
//            course.setDifficultyTypes(DifficultyTypes.valueOf(request.getDifficultyTypes()));
//        }
//        if (request.getTags() != null) course.setTags(request.getTags());
//        if (request.getImageUrl() != null) course.setImageUrl(request.getImageUrl());

        // 2. Update Modules and Sections if provided
        if (request.getModules() != null) {
            updateChapters(course, request.getModules());
        }

        Course saved = courseRepo.save(course);
        return courseMapper.maptoCourseResponse(saved);
    }

    private void updateChapters(Course course, Set<CourseChapterRequest> chapterRequest) {
        // Create a map of existing courseModules for quick lookup by ID
        Map<Long, Chapter> existingChaptersMap = course.getChapters().stream()
                .collect(Collectors.toMap(Chapter::getId, Function.identity()));

        // List to hold the final state of courseModules
        Set<Chapter> updatedChapters = new HashSet<>();

        for (CourseChapterRequest mReq : chapterRequest) {
            Chapter chapter;

            if (mReq.getId() != null && existingChaptersMap.containsKey(mReq.getId())) {
                // --- UPDATE EXISTING MODULE ---
                chapter = existingChaptersMap.get(mReq.getId());
                courseMapper.updateChapter(mReq, chapter);
//                chapter.setName(mReq.getName());
//                chapter.setDescription(mReq.getDescription());

                // Handle Sections for this courseModule
                if (mReq.getSections() != null) {
                    updateChapterSections(chapter, mReq.getSections());
                }
            } else {
                // --- CREATE NEW MODULE ---
                chapter = new Chapter();
                courseMapper.updateChapter(mReq, chapter);
//                chapter.setName(mReq.getName());
//                chapter.setDescription(mReq.getDescription());
                chapter.setCourse(course);

                // Handle Sections for the new courseModule
                if (chapter.getChapterSections() != null) {
//                    Set<ChapterSectionRequest> newChapterSections = mReq.getSections().stream().map(sReq -> {
//                        ChapterSectionRequest chapterSection = new ChapterSectionRequest();
//                        chapterSection.setTitle(sReq.getTitle());
//                        chapterSection.setContent(sReq.getContent());
//                        chapterSection.setResourceUrl(sReq.getResourceUrl());
//                        chapterSection.setResourceMediaType(sReq.getResourceMediaType());
//                        chapterSection.setOrderIndex(sReq.getOrderIndex());
//                        chapterSection.setTags(sReq.getTags());
//                        chapterSection.setChapter(chapter);
//                        return chapterSection;
//                    }).collect(Collectors.toSet());
                    chapter.getChapterSections().forEach(cs -> cs.setChapter(chapter));
                    chapter.setCourse(course);
                    chapter.setChapterSections(chapter.getChapterSections());
                }
            }
            updatedChapters.add(chapter);
        }

        // 3. Sync the collection:
        // Set the new list. Hibernate will delete orphans (courseModules in DB but not in updatedCourseModules)
        course.setChapters(updatedChapters);
    }

    private void updateChapterSections(Chapter chapter, Set<ChapterSectionRequest> chapterSectionRequests) {
        Map<Long, org.lucas.arbackend.entity.course.ChapterSection> existingSectionsMap = chapter.getChapterSections().stream()
                .collect(Collectors.toMap(org.lucas.arbackend.entity.course.ChapterSection::getId, Function.identity()));

        Set<ChapterSection> updatedSections = new HashSet<>();

        for (ChapterSectionRequest sReq : chapterSectionRequests) {
            ChapterSection section;

            if (sReq.getId() != null && existingSectionsMap.containsKey(sReq.getId())) {
                // --- UPDATE EXISTING SECTION ---
                section = existingSectionsMap.get(sReq.getId());
                courseMapper.updateChapterSection(sReq, section);
//                section.setTitle(sReq.getTitle());
//                section.setContent(sReq.getContent());
//                section.setResourceUrl(sReq.getResourceUrl());
//                section.setResourceMediaType(sReq.getResourceMediaType());
//                section.setOrderIndex(sReq.getOrderIndex());
//                section.setTags(sReq.getTags());
            } else {
                // --- CREATE NEW SECTION ---
                section = new ChapterSection();
                courseMapper.updateChapterSection(sReq, section);
//                section.setTitle(sReq.getTitle());
//                section.setContent(sReq.getContent());
//                section.setResourceUrl(sReq.getResourceUrl());
//                section.setResourceMediaType(sReq.getResourceMediaType());
//                section.setOrderIndex(sReq.getOrderIndex());
//                section.setTags(sReq.getTags());
                section.setChapter(chapter);
            }
            updatedSections.add(section);
        }

        // Sync the collection for chapterSectionRequests
        chapter.getChapterSections().addAll(updatedSections);
//        chapter.setChapterSections(updatedSections);
    }

    private void softDeleteCourse(Long courseId, Long orgId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
    }

    // Helper mapper
    // TODO: Create a CourseChapterResponse and ChapterSectionResponse DTO to be able to load them.
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .imageUrl(course.getImageUrl())
                .name(course.getName())
                .description(course.getDescription())
                .difficulty(course.getDifficultyTypes().name())
                .imageUrl(course.getImageUrl())
                .chapterResponses(course.getChapters().stream().map(m -> CourseChapterResponse.builder()
                        .id(m.getId())
                        .description(m.getDescription())
                        .sections(m.getChapterSections().stream().map(s -> ChapterSectionResponse.builder()
                                .id(s.getId())
                                .title(s.getTitle())
                                .duration(s.getDuration())
                                .resourceUrl(s.getResourceUrl())
                                .resourceMediaType(s.getResourceMediaType())
                                .build()).collect(Collectors.toSet()))
                        .build()).collect(Collectors.toSet()))
                .tags(course.getTags())
                .build();
    }

    private Organisation findOrganisation() {
        Long orgId = tenantProvider.get();

        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No organisation found for tenant id: [" + orgId +"]"));
    }
}
