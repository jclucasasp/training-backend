package org.lucas.arbackend.util;

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

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredSubscriptions() {
        List<OrganisationSubscription> expiredOrganisations = subRepo.findAllExpired();

        if (expiredOrganisations.isEmpty()){
            return;
        }

        for (OrganisationSubscription sub : expiredOrganisations) {
            sub.setStatus(0);
            if (sub.getOrganisation() != null) {
                cacheService.evictAuthUser(sub.getOrganisation().getEmail());
                if (sub.getOrganisation().getApiKey() != null){
                    cacheService.evictApiKey(sub.getOrganisation().getApiKey().getPrefix());
                }
                log.info("Subscription expired for organisation: {}", sub.getOrganisation().getEmail());
            }
        }
    }

}
