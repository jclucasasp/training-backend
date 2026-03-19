package org.lucas.arbackend.util.tenant;

import org.lucas.arbackend.entity.Organisation.Organisation;

public interface TenantEntity {
    Organisation getOrganisation();
    void setOrganisation(Organisation organisation);
}
