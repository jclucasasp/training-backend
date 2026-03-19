package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.util.tenant.TenantEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_attempt")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentQuizAttempt implements TenantEntity {

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
