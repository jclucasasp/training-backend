package org.lucas.arbackend.util.tenant;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantProvider {

    private final OrganisationRepository orgRepo;

    public Long get() {
        Long tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new IllegalStateException("No tenant id found in the Tenant Context");
        }

        return tenantId;
    }

    public Organisation getOrg() {
        return orgRepo.findById(get())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + get()));
    }
}
