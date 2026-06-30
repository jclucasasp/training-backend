package org.lucas.arbackend.entity.vr;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
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
    @Column(name = "vrs_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vrs_student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "vrs_section_id", nullable = false)
    private ChapterSection chapterSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vrs_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "vrs_device_id")
    private String deviceId;

    @Column(name = "vrs_headset_model")
    private String headsetModel;

    @Column(name = "vrs_stared_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "vrs_ended_at")
    private LocalDateTime endedAt;

    @Column(name = "vrs_duration_seconds")
    private Integer durationSeconds;

    @Column(name = "vrs_comfort_rating")
    private Integer comfortRating;

    @Column(name = "vrs_motion_sickness_reported")
    private Boolean motionSicknessReported;

    @Column(name = "vrs_session_quality_score", precision = 3, scale = 2)
    private BigDecimal sessionQualityScore;

    @Column(name = "vrs_avg_fps", precision = 4, scale = 1)
    private BigDecimal avgFps;

    @Column(name = "vrs_frame_drop_count")
    private Integer frameDropCount;

    @Column(name = "vrs_tracking_loss_count")
    private Integer trackingLossCount;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
