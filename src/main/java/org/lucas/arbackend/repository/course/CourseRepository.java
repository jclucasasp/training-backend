package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
