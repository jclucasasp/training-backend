package org.lucas.arbackend.util.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionChecker {

    private final OrganisationSubscriptionRepository subRepo;
    private final CacheService cacheService;

/**
 * Scheduled method to clean up expired subscriptions
 * Runs every hour at minute 0 (cron expression: 0 0 * * * *)
 * This method is transactional to ensure data consistency
 */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
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
            // Evict the authentication user from cache
//                cacheService.evictAuthUser(sub.getOrganisation().getEmail());
                cacheService.updateCache("auth_user", sub.getOrganisation().getEmail(), sub.getOrganisation());
            // If the organisation has an API key
                if (sub.getOrganisation().getApiKey() != null){
                // Evict the API key from cache
                    cacheService.evictApiKey(sub.getOrganisation().getApiKey().getPrefix());
                }
            // Log the expiration information
                log.info("Subscription expired for organisation: {}", sub.getOrganisation().getEmail());
            }
        }
    }

}
