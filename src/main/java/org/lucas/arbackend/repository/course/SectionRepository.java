package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
