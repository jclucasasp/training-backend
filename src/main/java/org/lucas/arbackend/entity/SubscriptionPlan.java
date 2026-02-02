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

    @Column(name = "sp_name")
    private String name;

    @Column(name = "sp_price")
    private Double price;

    @Column(name = "sp_course_limit")
    private Integer courseLimit;

    @Column(name = "sp_is_active")
    private Integer isActive;
}
