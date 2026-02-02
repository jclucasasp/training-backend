package org.lucas.arbackend.entity.Organisation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.SubscriptionPlan;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "organisation_subscription")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class OrganisationSubscription extends BaseEntity {

    @Id @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "os_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "os_org_id")
    private Organisation organisation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "os_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "os_status")
    private Integer status;
}