package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "section")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Section extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_id")
    private Long id;

    @Column(name = "s_title")
    private String title;

    @Column(name = "s_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "s_duration")
    private Integer duration;

    @Column(name = "s_resource_url")
    private String resourceUrl;

    @Column(name = "s_resource_media_type")
    private String resourceMediaType;

    @Column(name = "s_order_index")
    private Integer orderIndex;

    @Column(name = "s_tags", columnDefinition = "TEXT")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "s_module_id")
    private Module module;
}