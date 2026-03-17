package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmailAndOrganisationId(String email, Long orgId);

    Page<Staff> findAllByOrganisationId(Long orgId, Pageable pageable);

    Optional<Staff> findByEmail(String email);
}
