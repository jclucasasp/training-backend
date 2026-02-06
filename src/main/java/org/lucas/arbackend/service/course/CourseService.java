package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseCreateRequest;
import org.lucas.arbackend.dto.course.CourseResponse;
import org.lucas.arbackend.dto.course.CourseUpdateRequest;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.DifficultyTypes;
import org.lucas.arbackend.entity.course.Section;
import org.lucas.arbackend.entity.course.Module;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

   private final CourseRepository courseRepo;
    private final OrganisationRepository orgRepo;

    public CourseResponse createCourse(CourseCreateRequest request) {

        Long orgId = TenantContext.getCurrentTenant();

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

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
            List<Module> modules = request.getModules().stream().map(mReq -> {
                Module module = new Module();
                module.setName(mReq.getName());
                module.setCourse(course); // Link back to parent

                if (mReq.getSections() != null) {
                    List<Section> sections = mReq.getSections().stream().map(sReq -> {
                        Section section = new Section();
                        section.setTitle(sReq.getTitle());
                        section.setContent(sReq.getContent());
                        section.setResourceUrl(sReq.getResourceUrl());
                        section.setResourceMediaType(sReq.getResourceMediaType());
                        section.setOrderIndex(sReq.getOrderIndex());
                        section.setTags(sReq.getTags());
                        section.setModule(module); // Link back to parent
                        return section;
                    }).collect(Collectors.toList());
                    module.setSections(sections);
                }
                return module;
            }).collect(Collectors.toList());
            course.setModules(modules);
        }

        Course saved = courseRepo.save(course);
        return mapToResponse(saved);
    }

    public Page<CourseResponse> getPaginatedCourses(Pageable pageable) {

        Long orgId = TenantContext.getCurrentTenant();

        return courseRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(this::mapToResponse);
    }

    public CourseResponse updateCourse(Long courseId, CourseUpdateRequest request) {
        Long orgId = TenantContext.getCurrentTenant();

        Course course = courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(courseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found or does not belong to this organization"));

        // Update course fields
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        if (request.getDifficultyTypes() != null) {
            course.setDifficultyTypes(DifficultyTypes.valueOf(request.getDifficultyTypes()));
        }
        course.setTags(request.getTags());
        course.setImageUrl(request.getImageUrl());

        // Update modules and sections if provided
        if (request.getModules() != null) {
            // Remove existing modules
            course.getModules().clear();

            // Add new modules
            List<Module> modules = request.getModules().stream().map(mReq -> {
                Module module = new Module();
                module.setName(mReq.getName());
                module.setDescription(mReq.getDescription());
                module.setCourse(course); // Link back to parent

                if (mReq.getSections() != null) {
                    List<Section> sections = mReq.getSections().stream().map(sReq -> {
                        Section section = new Section();
                        section.setTitle(sReq.getTitle());
                        section.setContent(sReq.getContent());
                        section.setResourceUrl(sReq.getResourceUrl());
                        section.setResourceMediaType(sReq.getResourceMediaType());
                        section.setOrderIndex(sReq.getOrderIndex());
                        section.setTags(sReq.getTags());
                        section.setModule(module); // Link back to parent
                        return section;
                    }).collect(Collectors.toList());
                    module.setSections(sections);
                }
                return module;
            }).collect(Collectors.toList());
            course.setModules(modules);
        }

        Course saved = courseRepo.save(course);
        return mapToResponse(saved);
    }

    // Helper mapper
    // TODO: Create a ModuleResponse and SectionResponse DTO to be able to load them.
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .difficulty(course.getDifficultyTypes().name())
                .imageUrl(course.getImageUrl())
//                .modules(course.getModules())
                .tags(course.getTags())
                .build();
    }
}
