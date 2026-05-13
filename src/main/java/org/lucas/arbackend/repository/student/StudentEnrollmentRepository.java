package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {


    @Query("SELECT ste FROM StudentEnrollment ste WHERE ste.organisation.id = :orgId AND ste.student.id = :studentId AND ste.course.id = :courseId")
    Optional<StudentEnrollment> findByStudentIdAndCourseId(Long id, Long studentId, Long courseId);

    @Query("SELECT ste FROM StudentEnrollment ste WHERE ste.organisation.id = :orgId AND ste.student.id = :studentId AND ste.chapterSection.id = :sectionId")
    Optional<StudentEnrollment> findBySectionId(@Param("orgId") Long orgId, @Param("studentId") Long studentId, @Param("sectionId") Long sectionId);

    @Query("SELECT ste FROM StudentEnrollment ste WHERE ste.student.organisation.id = :orgId AND ste.student.studentNumber = :studentNumber")
    List<StudentEnrollment> findAllByStudentNumber(@Param("orgId") Long orgId, @Param("studentNumber") String studentNumber);

    @Query("SELECT ste FROM StudentEnrollment ste WHERE ste.student.organisation.id = :orgId AND ste.student.studentNumber = :studentNumber AND ste.course.slug = :courseSlug")
    Optional<StudentEnrollment> findByCourseSlug(@Param("orgId") Long orgId, @Param("studentNumber") String studentNumber, @Param("courseSlug") String courseSlug);
}
