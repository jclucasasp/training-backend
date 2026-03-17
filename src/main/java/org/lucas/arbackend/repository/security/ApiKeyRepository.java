package org.lucas.arbackend.repository.security;

import org.lucas.arbackend.entity.security.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByPrefix(String prefix);

    Optional<ApiKey> findByOrganisation_Id(Long orgId);
}
