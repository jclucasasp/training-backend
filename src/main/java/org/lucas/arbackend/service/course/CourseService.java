package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseMapper;
import org.lucas.arbackend.dto.course.helper.CourseRequest;
import org.lucas.arbackend.dto.course.helper.CourseResponse;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper mapper;

    public CourseResponse createCourse(CourseRequest request) {
        Course course = mapper.toEntity(request);
        return mapper.toResponse(courseRepository.save(course));
    }

    public CourseResponse getCourseById(Long id) {
        return mapper.toResponse(courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found")));
    }

    public CourseResponse getCourseByName(String courseName) {
        return mapper.toResponse(courseRepository.findByName(courseName)
                .orElseThrow(() -> new EntityNotFoundException("Course not found")));
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setDifficulty(request.getDifficulty());

        return mapper.toResponse(courseRepository.save(mapper.toEntity(request)));
    }

    public void deleteCourse(Long id) {

        courseRepository.deleteById(id);
    }
}
