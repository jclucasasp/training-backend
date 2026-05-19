package org.lucas.arbackend.entity.student;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress")
@SQLDelete(sql = "UPDATE student_progress SET ended_at = CURRENT_TIMESTAMP WHERE stp_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProgress extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stp_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stp_student_enrollment_id")
    private StudentEnrollment studentEnrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stp_chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stp_chapter_section_id")
    private ChapterSection chapterSection;

    @Builder.Default
    @Column(name = "stp_percentage")
    private BigDecimal percentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stp_is_completed")
    private Boolean isCompleted = false;

    @Column(name = "stp_last_access_at")
    private LocalDateTime lastAccessedAt;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}