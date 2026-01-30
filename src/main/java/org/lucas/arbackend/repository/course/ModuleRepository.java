package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}
