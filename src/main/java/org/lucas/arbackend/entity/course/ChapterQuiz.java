package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "chapter_quizzes")
@SQLDelete(sql = "UPDATE chapter_quizzes SET ended_at = CURRENT_TIMESTAMP WHERE cq_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChapterQuiz extends BaseEntity implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cq_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cha_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_org_id")
    Organisation organisation;

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }
}
