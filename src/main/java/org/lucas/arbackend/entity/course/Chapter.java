package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

@Entity
@Table(name = "chapter")
@SQLDelete(sql = "UPDATE chapter SET ended_at = CURRENT_TIMESTAMP WHERE cha_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chapter extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cha_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cha_org_id")
    Organisation organisation;

    @Column(name = "cha_name", unique = true, nullable = false)
    private String name;

    @Column(name = "cha_summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

     @Enumerated(EnumType.STRING)
    @Column(name = "cha_status")
    @Builder.Default
    private StatusTypes status = StatusTypes.DRAFT;

    @Column(name = "cha_total_time_minutes")
    private Integer totalTimeInMinutes;

    @Column(name = "cha_order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cha_course_id")
    private Course course;

    @Builder.Default
    @OneToMany(mappedBy = "chapter",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ChapterSection> chapterSections = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "chapter_quizzes",
        joinColumns = @JoinColumn(name = "cha_id"),
        inverseJoinColumns = @JoinColumn(name = "quiz_id")
    )
    private Set<Quiz> quizzes = new HashSet<>();

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}