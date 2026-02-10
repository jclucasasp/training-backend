package org.lucas.arbackend.repository;

import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgAddressRepository extends JpaRepository<OrgAddress, Long> {
}
