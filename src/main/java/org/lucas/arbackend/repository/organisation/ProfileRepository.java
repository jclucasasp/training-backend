package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
