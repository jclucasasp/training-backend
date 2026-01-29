package org.lucas.arbackend.dto.course;

import org.lucas.arbackend.dto.course.helper.CourseRequest;
import org.lucas.arbackend.dto.course.helper.CourseResponse;
import org.lucas.arbackend.entity.course.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request) {
        if (request == null) {
            return null;
        }

        return Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .build();
    }

    public CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .difficulty(course.getDifficulty())
                .createdAt(course.getCreatedAt())
                .endedAt(course.getEndedAt())
                .build();
    }

}
