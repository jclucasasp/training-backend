package org.lucas.arbackend.entity.vr.asset;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.util.tenant.TenantEntity;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
@SQLDelete(sql = "UPDATE vr_asset SET ended_at = NOW() WHERE vr_asset_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "vr_asset")
public class VRAsset extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_asset_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vr_asset_org_id")
    private Organisation organisation;

    @Column(name = "vr_asset_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "vr_asset_type")
    VRAssetType assetType;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
