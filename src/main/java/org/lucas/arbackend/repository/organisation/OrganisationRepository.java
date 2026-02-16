package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Page<Organisation> findAllByEmail(String email, Pageable pageable);

    @EntityGraph(value = "Organisation.withDetails", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Organisation> findByEmail(String email);

    @Override
    @NonNull
    @EntityGraph(value = "Organisation.withDetails", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Organisation> findById(@NonNull Long orgId);
}
