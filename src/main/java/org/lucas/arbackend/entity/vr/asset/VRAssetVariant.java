package org.lucas.arbackend.entity.vr.asset;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
@SQLDelete(sql = "UPDATE vr_asset_variant SET ended_at = NOW() WHERE vr_asset_var_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "vr_asset_variant")
public class VRAssetVariant extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_asset_var_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_asset_id")
    private VRAsset vrAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "vr_asset_var_platform", columnDefinition = "TEXT")
    private VRPlatformType platformType;

    @Column(name = "vr_asset_var_lod_level")
    private Integer lodLevel;

    @Column(name = "vr_asset_var_url")
    private String url;

    @Column(name = "vr_asset_var_file_size")
    private Integer fileSize;

    // SHA-256 for integrity verification and local headset caching
    @Column(name = "vr_asset_var_checksum")
    private String checksum;
}
