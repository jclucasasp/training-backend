package org.lucas.arbackend.repository.student;

import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.student.StudentEnrollment;
import org.lucas.arbackend.entity.student.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    // Optimized: Calculate course completion % in a single DB call
    @Query("""
    SELECT (COUNT(sp) * 100.0 / NULLIF((SELECT COUNT(cs) FROM ChapterSection cs WHERE cs.chapter.course.id = :courseId), 0))
    FROM StudentProgress sp
    WHERE sp.studentEnrollment.id = :enrollmentId AND sp.isCompleted = true
""")
    BigDecimal calculateCourseCompletion(@Param("courseId") Long courseId, @Param("enrollmentId") Long enrollmentId);

    // Optimized: Check if a student owns the enrollment before letting them update progress
    @Query("SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END FROM StudentEnrollment se " +
           "WHERE se.id = :enId AND se.student.organisation.id = :orgId")
    boolean isEnrollmentValidForOrg(@Param("enId") Long enrollmentId, @Param("orgId") Long orgId);

    Optional<StudentProgress> findByStudentEnrollmentAndChapterSection(StudentEnrollment enrollment, ChapterSection section);

    Long countByStudentEnrollmentAndIsCompletedTrue(StudentEnrollment enrollment);
}
