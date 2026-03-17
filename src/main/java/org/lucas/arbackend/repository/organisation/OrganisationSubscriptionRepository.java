package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganisationSubscriptionRepository extends JpaRepository<OrganisationSubscription, Long> {
    // Find the current active subscription for an org
    @Cacheable(value = "active_subscriptions", key = "#orgId", unless = "#result == null")
    // The () is important, else the AND will override the OR statement
    @Query("SELECT os FROM OrganisationSubscription os WHERE os.organisation.id = :orgId AND os.status = 1 AND (os.endedAt IS NULL OR os.endedAt < CURRENT_TIMESTAMP)")
    Optional<OrganisationSubscription> findActiveByOrganisationId(@Param("orgId") Long orgId);

    @Query("SELECT os FROM OrganisationSubscription os WHERE os.endedAt < CURRENT_TIMESTAMP AND os.status = 1")
    List<OrganisationSubscription> findAllExpired();
}
