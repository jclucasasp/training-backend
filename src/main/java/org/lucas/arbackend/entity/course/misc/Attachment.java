package org.lucas.arbackend.entity.course.misc;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "attachment")
@SQLDelete(sql = "UPDATE student SET ended_at = CURRENT_TIMESTAMP WHERE stu_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "att_name")
    private String name;

    @Column(name = "att_url")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "att_chs_id")
    private ChapterSection chapterSection;
}
