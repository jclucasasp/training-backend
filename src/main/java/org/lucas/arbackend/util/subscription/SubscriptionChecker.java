package org.lucas.arbackend.util.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionChecker {

    private final OrganisationSubscriptionRepository subRepo;
    private final OrganisationRepository orgRepo;
    private final RoleRepository roleRepo;
    private final CacheService cacheService;

/**
 * Scheduled method to clean up expired subscriptions
 * Runs every hour at minute 0 (cron expression: 0 0 * * * *)
 * This method is transactional to ensure data consistency
 */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    // TODO: Need to send an email via Rabbit MQ to admin.
    public void cleanExpiredSubscriptions() {
    // Retrieve all expired subscriptions from the repository
        List<OrganisationSubscription> expiredOrganisations = subRepo.findAllExpired();

    // If no expired subscriptions found, exit the method
        if (expiredOrganisations.isEmpty()){
            return;
        }

    // Process each expired subscription
        for (OrganisationSubscription sub : expiredOrganisations) {
        // Set the subscription status to inactive (0)
            sub.setStatus(0);
        // If the subscription has an associated organisation
            if (sub.getOrganisation() != null) {
                // Set the organisation's role to inactive
                Organisation org = sub.getOrganisation();
                sub.setEndedAt(LocalDateTime.now());
                sub.setStatus(0);
                cacheService.evictAuthUser(org.getEmail());

                org.setRole(roleRepo.findByRoleName(RoleTypes.INACTIVE));
                orgRepo.save(org);
                subRepo.save(sub);
            // Log the expiration information
                log.info("Subscription expired for organisation: {}", org.getEmail());
            }
        }
    }

}
