package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.util.tenant.TenantEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_attempt")
@SQLDelete(sql = "UPDATE student_quiz_attempt SET ended_at = CURRENT_TIMESTAMP WHERE sqa_id = :id")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentQuizAttempt extends BaseEntity implements TenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sqa_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_org_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "sqa_score", nullable = false)
    private BigDecimal score;

    @Column(name = "sqa_is_passed")
    private boolean isPassed;

    @Column(name = "sqa_submitted_answers_json", columnDefinition = "TEXT")
    private String submittedAnswersJson; // Stores the raw submission data

    @Column(name = "sqa_started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "sqa_completed_at")
    private LocalDateTime completedAt;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
