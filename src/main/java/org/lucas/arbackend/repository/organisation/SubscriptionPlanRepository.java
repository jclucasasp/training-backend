package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

}
