package org.lucas.arbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sp_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sp_plan")
    private PlanTypes plan;

    @Column(name = "sp_price")
    private Double price;

    @Column(name = "sp_course_limit")
    private Integer courseLimit;

    @Column(name = "sp_is_active")
    private Integer isActive;
}
