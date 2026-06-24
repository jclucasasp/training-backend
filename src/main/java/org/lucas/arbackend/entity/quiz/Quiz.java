package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quiz")
@NamedEntityGraph(
    name = "Quiz.questionsAndOptions",
    attributeNodes = @NamedAttributeNode(value = "questions", subgraph = "questions-subgraph"),
    subgraphs = @NamedSubgraph(
        name = "questions-subgraph",
        attributeNodes = @NamedAttributeNode("options")
    )
)
//@SQLDelete(sql = "UPDATE student SET ended_at = CURRENT_TIMESTAMP WHERE stu_id = ?")
@SQLDelete(sql = "UPDATE quiz SET ended_at = CURRENT_TIMESTAMP WHERE quiz_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_org_id")
    Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_course_id")
    private Course course;

    @Column(name = "quiz_title", nullable = false)
    private String title;

    @Column(name = "quiz_max_attempts", nullable = false)
    private Integer maxAttempts = 0;

    @Column(name = "quiz_passing_score")
    private Integer passingScore;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentQuiz> studentQuizzes = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChapterQuiz> chapterQuizzes = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuizQuestion> questions = new HashSet<>();

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }
}
