package org.lucas.arbackend.entity.student;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Chapter;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student_enrollment")
@SQLDelete(sql = "UPDATE student_enrollment SET ended_at = CURRENT_TIMESTAMP WHERE ste_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentEnrollment extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ste_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ste_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ste_student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ste_course_id")
    private Course course;

    @CreatedDate
    @Column(name = "ste_enrolled_at", updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "ste_completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "ste_total_progress", nullable = false)
    private BigDecimal totalProgress = BigDecimal.ZERO;

    @OneToMany(mappedBy = "studentEnrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentProgress> studentProgresses = new HashSet<>();

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}