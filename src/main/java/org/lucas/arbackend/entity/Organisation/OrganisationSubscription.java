package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "organisation_subscription")
@SQLDelete(sql = "UPDATE organisation_subscription SET ended_at = CURRENT_TIMESTAMP WHERE osu_org_id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class OrganisationSubscription extends BaseEntity {

    @Id
    @Column(name = "osu_org_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "osu_org_id")
    @JsonIgnore
    private Organisation organisation;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "osu_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "osu_subscription_amount", nullable = false)
    private Double subscriptionAmount;

    @Column(name = "osu_status")
    private Integer status;
}