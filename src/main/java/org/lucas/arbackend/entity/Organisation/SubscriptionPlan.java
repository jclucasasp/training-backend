package org.lucas.arbackend.entity.Organisation;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.PlanTypes;

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
    @Column(name = "sup_plan", columnDefinition = "ENUM('BASIC', 'STANDARD', 'PREMIUM')")
    private PlanTypes plan;

    @Column(name = "sup_price")
    private Double price;

    @Column(name = "sup_course_limit")
    private Integer courseLimit;

    @Column(name = "sup_is_active")
    private Integer isActive;
}
