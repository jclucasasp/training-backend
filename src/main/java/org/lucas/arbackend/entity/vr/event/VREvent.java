package org.lucas.arbackend.entity.vr.event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.vr.VRSession;
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
    @JoinColumn (name = "vr_eve_session_id", nullable = false)
    private VRSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_eve_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "vr_eve_event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VREventType eventType; // GAZE, INTERACT, GRAB, TELEPORT, COLLISION, COMPLETION_CONDITION_MET

    @Column(name = "vr_eve_timestamp", nullable = false)
    private LocalDateTime timestamp; // Event time (not session time, for precision)

    @Column(name = "vr_eve_position_x", precision = 10, scale = 4)
    private BigDecimal positionX;

    @Column(name = "vr_eve_position_y", precision = 10, scale = 4)
    private BigDecimal positionY;

    @Column(name = "vr_eve_position_z", precision = 10, scale = 4)
    private BigDecimal positionZ;

    @Column(name = "vr_eve_rotation_x", precision = 10, scale = 4)
    private BigDecimal rotationX;

    @Column(name = "vr_eve_rotation_y", precision = 10, scale = 4)
    private BigDecimal rotationY;

    @Column(name = "vr_eve_rotation_z", precision = 10, scale = 4)
    private BigDecimal rotationZ;

    @Column(name = "vr_eve_target_object_id", length = 100)
    private String targetObjectId;  // Matches SceneObject.objectId from your SceneConfig

    @Column(name = "vr_eve_duration_ms")
    private Integer durationInMilliseconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vr_eve_metadata")
    private String metadataJson;

    @Column(name = "vr_eve_hand")
    @Enumerated(EnumType.STRING)
    private VRHandType hand;

    @Column(name = "vr_eve_sequence_number")
    private Long sequenceNumber;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
