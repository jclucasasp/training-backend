package org.lucas.arbackend.entity.vr.competency.embedded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterionAssessmentResult {
    private Long criterionId;
    private Boolean passed;
    private String actualValue;
    private String feedback;
}
