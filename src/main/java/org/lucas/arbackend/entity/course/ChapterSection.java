package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.misc.Attachment;
import org.lucas.arbackend.entity.course.misc.SceneConfig;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapter_section")
@SQLDelete(sql = "UPDATE chapter_section SET ended_at = CURRENT_TIMESTAMP WHERE chs_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChapterSection extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chs_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chs_org_id")
    Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chs_chapter_id")
    private Chapter chapter;

    @Column(name = "chs_title", unique = true, nullable = false)
    private String title;

    @Column(name = "chs_content", nullable = false, columnDefinition = "TEXT")
    private String content;

     @Column(name = "chs_is_preview")
    private boolean isPreview = false;

     @Column(name = "chs_subtitles_url")
    private String subtitlesUrl;

    @Column(name = "chs_duration_minutes")
    private Integer durationInMinutes;

    @Column(name = "chs_resource_url")
    private String resourceUrl;

    @Column(name = "chs_resource_media_type")
    private String resourceMediaType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chs_scene_config", columnDefinition = "LONGTEXT")
    private SceneConfig sceneConfig;

    @Column(name = "chs_tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "chs_order_index")
    private Integer orderIndex;

    @OneToMany(mappedBy = "chapterSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}