package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Staff;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    @Cacheable(value = "staff_users", key = "#email", unless = "#result == null")
    Optional<Staff> findByEmail(String email);

    Page<Staff> findByOrganisationId(Long orgId, Pageable pageable);
}
