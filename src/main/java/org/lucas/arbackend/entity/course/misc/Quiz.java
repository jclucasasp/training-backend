package org.lucas.arbackend.entity.course.misc;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.StudentQuiz;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quiz")
@SQLDelete(sql = "UPDATE student SET ended_at = CURRENT_TIMESTAMP WHERE stu_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_title", nullable = false)
    private String title;

    @Column(name = "quiz_passing_score")
    private Integer passingScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_org_id")
    Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_course_id")
    private Course course;

    @OneToMany(mappedBy = "quiz")
    private Set<StudentQuiz> studentQuizzes = new HashSet<>();

    @OneToMany(mappedBy = "quiz")
    private Set<ChapterQuiz> chapterQuizzes = new HashSet<>();
}
