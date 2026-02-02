package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    // Quickly find sections for a module, ordered by index
    List<Section> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
}
