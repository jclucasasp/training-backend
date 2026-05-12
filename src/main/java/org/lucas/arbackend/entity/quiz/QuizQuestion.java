package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_question")
@SQLDelete(sql = "UPDATE quiz_question SET ended_at = CURRENT_TIMESTAMP WHERE qq_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestion extends BaseEntity implements TenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qq_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qq_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qq_quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "qq_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "qq_type", nullable = false, columnDefinition = "ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE') DEFAULT 'MULTIPLE_CHOICE'")
    private QuizTypes type = QuizTypes.MULTIPLE_CHOICE;

    @Column(name = "qq_order_index")
    private Integer orderIndex;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuizQuestionOption> options = new ArrayList<>();

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
