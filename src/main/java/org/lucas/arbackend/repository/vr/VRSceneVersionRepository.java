package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

    // Find all versions by scene ID
@Repository
public interface VRSceneVersionRepository extends JpaRepository<VRSceneVersion, Long> {
    List<VRSceneVersion> findAllBySceneId(Long sceneId);

    // This method uses a custom JPQL query to update the database
    // It sets the isActive flag to false for all versions of a specific scene
 // Indicates that this query is an update or delete operation
    // Helps flag sibling versions inactive when a new version is promoted // JPQL query to update versions
    @Modifying // Method to deactivate all versions for a given scene ID
    @Query("UPDATE VRSceneVersion v SET v.isActive = false WHERE v.scene.id = :sceneId")
    void deactivateAllVersionsForScene(@Param("sceneId") Long sceneId);
}
