package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.SubscriptionPlan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "organisation_subscription")
//@SQLDelete(sql = "UPDATE organisation_subscription SET ended_at = CURRENT_TIMESTAMP WHERE os_org_id = ?")
//@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class OrganisationSubscription extends BaseEntity {

    @Id
    @Column(name = "os_org_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "os_org_id")
    @JsonIgnore
    private Organisation organisation;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "os_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "os_status")
    private Integer status;
}