package org.lucas.arbackend.entity.vr;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.util.tenant.TenantEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vr_event")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VREvent extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "vre_session_id", nullable = false)
    private VRSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vre_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "vre_event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VREventType eventType; // GAZE, INTERACT, GRAB, TELEPORT, COLLISION, COMPLETION_CONDITION_MET

    @Column(name = "vre_timestamp", nullable = false)
    private LocalDateTime timestamp; // Event time (not session time, for precision)

    @Column(name = "vre_position_x", precision = 10, scale = 4)
    private BigDecimal positionX;

    @Column(name = "vre_position_y", precision = 10, scale = 4)
    private BigDecimal positionY;

    @Column(name = "vre_position_z", precision = 10, scale = 4)
    private BigDecimal positionZ;

    @Column(name = "vre_rotation_x", precision = 10, scale = 4)
    private BigDecimal rotationX;

    @Column(name = "vre_rotation_y", precision = 10, scale = 4)
    private BigDecimal rotationY;

    @Column(name = "vre_rotation_z", precision = 10, scale = 4)
    private BigDecimal rotationZ;

    @Column(name = "vre_target_object_id", length = 100)
    private String targetObjectId;  // Matches SceneObject.objectId from your SceneConfig

    @Column(name = "vre_duration_ms")
    private Integer durationInMilliseconds;

    @Column(name = "vre_metadata")
    private String metadataJson;

    @Column(name = "vre_hand")
    @Enumerated(EnumType.STRING)
    private VRHandType hand;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
