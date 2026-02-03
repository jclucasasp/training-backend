package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Page<Organisation> findAllByEmail(String email, Pageable pageable);

    Optional<Organisation> findByEmail(String email);
}
