package org.lucas.arbackend.repository.relationship;

import org.lucas.arbackend.entity.relationship.OrgApiRel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgApiRelRepository extends JpaRepository<OrgApiRel, Long> {
    Optional<OrgApiRel> findByApiKeyValue(String apiKey);
}
