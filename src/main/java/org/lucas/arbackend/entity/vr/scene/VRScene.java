package org.lucas.arbackend.entity.vr.scene;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.lucas.arbackend.util.tenant.TenantEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
@SQLDelete(sql = "UPDATE vr_scene SET ended_at = NOW() WHERE vr_sce_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "vr_scene")
public class VRScene extends BaseEntity implements TenantEntity {
    @Id() @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_sce_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vr_sce_org_id")
    Organisation organisation;

    @Column(name = "vr_sce_title")
    private String title;

    @Column(name = "vr_sce_description", columnDefinition = "TEXT")
    private String description;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
