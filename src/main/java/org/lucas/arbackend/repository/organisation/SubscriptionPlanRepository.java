package org.lucas.arbackend.repository.organisation;

import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.Organisation.PlanTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByPlan(PlanTypes subscriptionPlan);
}
