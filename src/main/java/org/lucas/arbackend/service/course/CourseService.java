package org.lucas.arbackend.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.course.CourseMapper;
import org.lucas.arbackend.dto.course.helper.CourseRequest;
import org.lucas.arbackend.dto.course.helper.CourseResponse;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.course.CourseRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseMapper mapper;

    public CourseResponse createCourse(CourseRequest request) {
        Course course = mapper.toEntity(request);
        return mapper.toResponse(courseRepository.save(course));
    }

     public List<CourseResponse> getCoursesForStudent(String studentNumber) {
        // Find the student to get their related org_id
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // Query courses filtered by that org_id
        return courseRepository.findAllByOrganisationId(student.getOrganisation().getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
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
