package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

    Optional<StudentEnrollment> findByStudentIdAndCourseId(Long id, Long courseId);

    Optional<StudentEnrollment> findByStudentAndChapterSection(Student student, ChapterSection section);

    List<StudentEnrollment> findAllByStudentOrganisationIdAndStudentStudentNumber(Long orgId, String studentNumber);

    Optional<StudentEnrollment> findByStudentOrganisationIdAndStudentStudentNumberAndCourseSlug(Long id, String studentNumber, String courseSlug);
}
