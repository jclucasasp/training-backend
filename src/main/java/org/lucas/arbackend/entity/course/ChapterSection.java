package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "chapter_section")
@SQLDelete(sql = "UPDATE chapter_section SET ended_at = CURRENT_TIMESTAMP WHERE chs_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChapterSection extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chs_id")
    private Long id;

    @Column(name = "chs_title", unique = true, nullable = false)
    private String title;

    @Column(name = "chs_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "chs_duration_minutes")
    private Integer durationInMinutes;

    @Column(name = "chs_resource_url")
    private String resourceUrl;

    @Column(name = "chs_resource_media_type")
    private String resourceMediaType;

    @Column(name = "chs_order_index")
    private Integer orderIndex;

    @Column(name = "chs_tags", columnDefinition = "TEXT")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chs_chapter_id")
    private Chapter chapter;
}