package org.lucas.arbackend.entity.vr;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vr_session")
@SQLDelete(sql = "UPDATE vr_session SET deleted_at = CURRENT_TIMESTAMP WHERE vrs_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class VRSession extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_ses_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_ses_student_number", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "vr_ses_section_id", nullable = false)
    private ChapterSection chapterSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_ses_org_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_ses_scene_version_id", nullable = true)
    private VRSceneVersion sceneVersion;

    @Column(name = "vr_ses_device_id")
    private String deviceId;

    @Column(name = "vr_ses_headset_model")
    private String headsetModel;

    @Column(name = "vr_ses_stared_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "vr_ses_ended_at")
    private LocalDateTime endedAt;

    @Column(name = "vr_ses_duration_seconds")
    private Integer durationSeconds;

    @Column(name = "vr_ses_comfort_rating")
    private Integer comfortRating;

    @Column(name = "vr_ses_motion_sickness_reported")
    private Boolean motionSicknessReported;

    @Column(name = "vr_ses_session_quality_score", precision = 3, scale = 2)
    private BigDecimal sessionQualityScore;

    @Column(name = "vr_ses_avg_fps", precision = 4, scale = 1)
    private BigDecimal avgFps;

    @Column(name = "vr_ses_frame_drop_count")
    private Integer frameDropCount;

    @Column(name = "vr_ses_tracking_loss_count")
    private Integer trackingLossCount;

     @Column(name = "vr_ses_interaction_count")
    private Integer interactionCount;

    @Column(name = "vr_ses_hint_request_count")
    private Integer hintRequestCount;

    @Column(name = "vr_ses_failure_count")
    private Integer failureCount;

    @Column(name = "vr_ses_completion_condition_met")
    private Boolean completionConditionMet;

    @Column(name = "vr_ses_completion_time_ms")
    private Long completionTimeMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "vr_ses_status", nullable = false)
    private VRSessionStatus status = VRSessionStatus.IN_PROGRESS;

    @Column(name = "vr_ses_last_sequence_number")
    private Long lastSequenceNumber;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
