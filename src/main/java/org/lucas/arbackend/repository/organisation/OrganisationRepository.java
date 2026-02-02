package org.lucas.arbackend.repository.organisation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Page<Organisation> findByEmail(String email);

    Optional<Organisation> findByOrgEmail(String email);
}
