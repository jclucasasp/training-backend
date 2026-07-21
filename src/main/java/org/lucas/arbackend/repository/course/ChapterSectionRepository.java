package org.lucas.arbackend.repository.course;

import jakarta.validation.constraints.NotNull;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterSectionRepository extends JpaRepository<ChapterSection, Long> {

    @Query("SELECT s FROM ChapterSection s " +
           "JOIN FETCH s.chapter c " +
           "JOIN FETCH c.course co " +
           "WHERE s.id = :sectionId AND c.id = :chapterId AND co.id = :courseId AND co.organisation.id = :orgId")
    Optional<ChapterSection> findWithContext(
        @Param("courseId") Long courseId,
        @Param("chapterId") Long chapterId,
        @Param("orgId") Long orgId,
        @Param("sectionId") Long sectionId
    );

    Optional<ChapterSection> findByIdAndOrganisationId(Long sectionId, Long orgId);
}
