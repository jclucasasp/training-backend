package org.lucas.arbackend.entity.vr.scene;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.lucas.arbackend.entity.base.BaseEntity;

@Entity
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE vr_scene_version SET ended_at = NOW() WHERE id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "vr_scene_version")
public class VRSceneVersion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_sce_ver_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_sce_id")
    private VRScene scene;

    @Column(name = "vr_sce_version_tag")
    private String versionTag;

    @Column(name = "vr_sce_change_log", columnDefinition = "TEXT")
    private String changeLog;

    @Column(name = "vr_sce_is_active")
    private boolean isActive;

    @Column(name = "vr_sce_environmental_file_url")
    private String environmentalFileUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hierarchy_json", columnDefinition = "LONGTEXT")
    private String hierarchyJson;
}
