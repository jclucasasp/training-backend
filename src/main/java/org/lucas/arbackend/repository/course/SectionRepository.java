package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.ChapterSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SectionRepository extends JpaRepository<ChapterSection, Long> {

    // Quickly find chapterSections for a courseModule, ordered by index
    Set<ChapterSection> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
}
