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
import java.util.Set;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    // Finds the specific progress row for a single section
    Optional<StudentProgress> findByStudentEnrollmentIdAndChapterSectionId(Long enrollmentId, Long sectionId);

    // Easily pull all completed sections for a course to give the frontend its checkmarks
    @Query("SELECT sp.chapterSection.id FROM StudentProgress sp WHERE sp.studentEnrollment.id = :enrollmentId AND sp.isCompleted = true")
    Set<Long> findCompletedSectionIdsByEnrollmentId(@Param("enrollmentId") Long enrollmentId);
}
