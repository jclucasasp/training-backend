package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chapter")
@SQLDelete(sql = "UPDATE chapter SET ended_at = CURRENT_TIMESTAMP WHERE cha_id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chapter extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cha_id")
    private Long id;

    @Column(name = "cha_name", unique = true, nullable = false)
    private String name;

    @Column(name = "cha_summary", nullable = false)
    private String summary;

    @Column(name = "cha_total_time_minutes")
    private Integer totalTimeInMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cha_course_id")
    private Course course;

    @Builder.Default
    @OneToMany(mappedBy = "chapter",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<ChapterSection> chapterSections = new HashSet<>();
}