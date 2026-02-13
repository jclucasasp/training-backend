package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.DifficultyTypes;
import org.lucas.arbackend.entity.course.Section;
import org.lucas.arbackend.entity.course.Module;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.util.TenantContext;
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

    private final CourseRepository courseRepo;
    private final OrganisationRepository orgRepo;
    private final TenantProvider tenantProvider;

    public CourseResponse createCourse(CourseRequest request) {

        Organisation org = tenantProvider.get();

        // Map DTO to Entity
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setDifficultyTypes(DifficultyTypes.valueOf(request.getDifficultyTypes()));
        course.setTags(request.getTags());
        course.setImageUrl(request.getImageUrl());
        course.setOrganisation(org);

        // Map Modules & Sections
        if (request.getModules() != null) {
            Set<Module> modules = request.getModules().stream().map(mReq -> {
                Module module = new Module();
                module.setName(mReq.getName());
                module.setCourse(course); // Link back to parent

                if (mReq.getSections() != null) {
                    Set<Section> sections = mReq.getSections().stream().map(sReq -> {
                        Section section = new Section();
                        section.setTitle(sReq.getTitle());
                        section.setContent(sReq.getContent());
                        section.setResourceUrl(sReq.getResourceUrl());
                        section.setResourceMediaType(sReq.getResourceMediaType());
                        section.setOrderIndex(sReq.getOrderIndex());
                        section.setTags(sReq.getTags());
                        section.setModule(module); // Link back to parent
                        return section;
                    }).collect(Collectors.toSet());
                    module.setSections(sections);
                }
                return module;
            }).collect(Collectors.toSet());
            course.setModules(modules);
        }

        Course saved = courseRepo.save(course);
        return mapToResponse(saved);
    }

    public Page<CourseResponse> getPaginatedCourses(Pageable pageable) {

        Long orgId = tenantProvider.get().getId();

        return courseRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(this::mapToResponse);
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Long orgId = tenantProvider.get().getId();

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));

        // 1. Update simple Course fields
        if (request.getName() != null) course.setName(request.getName());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getDifficultyTypes() != null) {
            course.setDifficultyTypes(DifficultyTypes.valueOf(request.getDifficultyTypes()));
        }
        if (request.getTags() != null) course.setTags(request.getTags());
        if (request.getImageUrl() != null) course.setImageUrl(request.getImageUrl());

        // 2. Update Modules and Sections if provided
        if (request.getModules() != null) {
            updateModules(course, request.getModules());
        }

        Course saved = courseRepo.save(course);
        return mapToResponse(saved);
    }

    private void updateModules(Course course, Set<Module> moduleRequests) {
        // Create a map of existing modules for quick lookup by ID
        Map<Long, Module> existingModulesMap = course.getModules().stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // List to hold the final state of modules
        Set<Module> updatedModules = new HashSet<>();

        for (Module mReq : moduleRequests) {
            Module module;

            if (mReq.getId() != null && existingModulesMap.containsKey(mReq.getId())) {
                // --- UPDATE EXISTING MODULE ---
                module = existingModulesMap.get(mReq.getId());
                module.setName(mReq.getName());
                module.setDescription(mReq.getDescription());

                // Handle Sections for this module
                if (mReq.getSections() != null) {
                    updateSections(module, mReq.getSections());
                }
            } else {
                // --- CREATE NEW MODULE ---
                module = new Module();
                module.setName(mReq.getName());
                module.setDescription(mReq.getDescription());
                module.setCourse(course);

                // Handle Sections for the new module
                if (mReq.getSections() != null) {
                    Set<Section> newSections = mReq.getSections().stream().map(sReq -> {
                        Section section = new Section();
                        section.setTitle(sReq.getTitle());
                        section.setContent(sReq.getContent());
                        section.setResourceUrl(sReq.getResourceUrl());
                        section.setResourceMediaType(sReq.getResourceMediaType());
                        section.setOrderIndex(sReq.getOrderIndex());
                        section.setTags(sReq.getTags());
                        section.setModule(module);
                        return section;
                    }).collect(Collectors.toSet());
                    module.setSections(newSections);
                }
            }
            updatedModules.add(module);
        }

        // 3. Sync the collection:
        // Set the new list. Hibernate will delete orphans (modules in DB but not in updatedModules)
        course.setModules(updatedModules);
    }

    private void updateSections(Module module, Set<Section> sectionRequests) {
        Map<Long, Section> existingSectionsMap = module.getSections().stream()
                .collect(Collectors.toMap(Section::getId, Function.identity()));

        Set<Section> updatedSections = new HashSet<>();

        for (Section sReq : sectionRequests) {
            Section section;

            if (sReq.getId() != null && existingSectionsMap.containsKey(sReq.getId())) {
                // --- UPDATE EXISTING SECTION ---
                section = existingSectionsMap.get(sReq.getId());
                section.setTitle(sReq.getTitle());
                section.setContent(sReq.getContent());
                section.setResourceUrl(sReq.getResourceUrl());
                section.setResourceMediaType(sReq.getResourceMediaType());
                section.setOrderIndex(sReq.getOrderIndex());
                section.setTags(sReq.getTags());
            } else {
                // --- CREATE NEW SECTION ---
                section = new Section();
                section.setTitle(sReq.getTitle());
                section.setContent(sReq.getContent());
                section.setResourceUrl(sReq.getResourceUrl());
                section.setResourceMediaType(sReq.getResourceMediaType());
                section.setOrderIndex(sReq.getOrderIndex());
                section.setTags(sReq.getTags());
                section.setModule(module);
            }
            updatedSections.add(section);
        }

        // Sync the collection for sections
        module.setSections(updatedSections);
    }

    private void softDeleteCourse(Long courseId, Long orgId) {

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepo.delete(course);
    }

    // Helper mapper
    // TODO: Create a ModuleResponse and SectionResponse DTO to be able to load them.
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .imageUrl(course.getImageUrl())
                .name(course.getName())
                .description(course.getDescription())
                .difficulty(course.getDifficultyTypes().name())
                .imageUrl(course.getImageUrl())
                .modules(course.getModules().stream().map(m -> ModuleResponse.builder()
                        .id(m.getId())
                        .description(m.getDescription())
                        .sections(m.getSections().stream().map(s -> SectionResponse.builder()
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
}
