package org.lucas.arbackend.util;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantProvider {

    public Long get() {
        Long tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new IllegalStateException("No tenant id found in the Tenant Context");
        }

        return tenantId;
    }
}
