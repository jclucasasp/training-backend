package org.lucas.arbackend.repository.security;

import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
