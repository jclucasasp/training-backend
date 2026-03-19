package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.util.tenant.TenantEntity;

@Entity
@Table(name = "quiz_question_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionOption implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qto_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qto_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qto_question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "qto_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "qto_is_correct")
    private boolean isCorrect;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
