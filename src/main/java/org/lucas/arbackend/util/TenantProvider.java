package org.lucas.arbackend.util;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantProvider {

    private final OrganisationRepository repository;

    public Organisation get() {
        Long tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new IllegalStateException("No organisation id found in the Tenant Context");
        }

        return repository.findById(tenantId).orElseThrow(() -> new EntityNotFoundException("No organisation found with id: " + tenantId));
    }
}
