package org.lucas.arbackend.entity.student;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "student_progress")
@SQLDelete(sql = "UPDATE student_progress SET ended_at = CURRENT_TIMESTAMP WHERE sp_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProgress extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sp_student_enrollment_id")
    private StudentEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sp_section_id")
    private ChapterSection chapterSection;

    @Column(name = "sp_percentage")
    private Double percentage;
}