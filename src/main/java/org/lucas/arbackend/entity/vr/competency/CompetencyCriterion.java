package org.lucas.arbackend.entity.vr.competency;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;

@Entity
@Getter
@Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE competency_criteria SET ended_at = NOW() WHERE criterion_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "competency_criteria")
public class CompetencyCriterion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_crit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_crit_comp_id", nullable = false)
    private Competency competency;

    @Column(name = "comp_crit_description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "comp_crit_metric_type", nullable = false)
    private MetricType metricType;

    @Column(name = "comp_crit_threshold_value", nullable = false)
    private String thresholdValue; // e.g., "<= 10.0" or "TRUE"

    @Column(name = "comp_crit_weight", nullable = false)
    private Double weight; // Weighting factor for overall score calculation
}
