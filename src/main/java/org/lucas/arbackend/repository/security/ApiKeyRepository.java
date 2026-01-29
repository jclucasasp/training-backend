package org.lucas.arbackend.repository.security;

import org.lucas.arbackend.entity.security.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
}
