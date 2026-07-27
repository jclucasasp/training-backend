package org.lucas.arbackend.entity.vr.competency;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.vr.VRSession;
import org.lucas.arbackend.entity.vr.competency.embedded.CriterionAssessmentResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE competency_assessments SET ended_at = NOW() WHERE comp_assessment_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "vr_competency_assessments")
public class CompetencyAssessment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_asse_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_asse_session_id", nullable = false)
    private VRSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_asse_student_number", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_asse_comp_id", nullable = false)
    private Competency competency;

    @Column(name = "comp_asse_score", nullable = false)
    private Double score;

    @Column(name = "comp_asse_passed", nullable = false)
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "comp_ass_assessed_by", nullable = false)
    private AssessedBy assessedBy;

    @ElementCollection
    @CollectionTable(name = "assessment_criteria_results", joinColumns = @JoinColumn(name = "comp_asse_id"))
    @Builder.Default
    private List<CriterionAssessmentResult> criteriaResults = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime assessedAt;
}
