package org.lucas.arbackend.entity.Organisation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sup_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sup_plan", columnDefinition = "ENUM('MONTHLY', 'YEARLY')")
    private PlanTypes plan;

    @Column(name = "sup_price")
    private Double price;

    @Column(name = "sup_course_limit")
    private Integer courseLimit;

    @Column(name = "sup_is_active")
    private Integer isActive;
}
