package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationSubscriptionRepository extends JpaRepository<OrganisationSubscription, Long> {
    // Find the current active subscription for an org
    @Query("SELECT os FROM OrganisationSubscription os WHERE os.orgId = :orgId AND os.status = true AND os.endDate > CURRENT_TIMESTAMP")
    Optional<OrganisationSubscription> findActiveSubscription(@Param("orgId") Long orgId);
}
