package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.Asset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    @EntityGraph(attributePaths = {"course"})
    List<Asset> findAll();
}
