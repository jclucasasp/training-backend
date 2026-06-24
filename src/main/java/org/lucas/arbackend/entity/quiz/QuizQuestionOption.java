package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "quiz_question_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE quiz_question_option SET ended_at = CURRENT_TIMESTAMP WHERE qto_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
public class QuizQuestionOption extends BaseEntity implements TenantEntity {
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
    private boolean correct;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
