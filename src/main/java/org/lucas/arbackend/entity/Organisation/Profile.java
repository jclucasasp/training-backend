package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "profile")
@SQLDelete(sql = "UPDATE profile SET ended_at = CURRENT_TIMESTAMP WHERE p_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile extends BaseEntity {
    @Id
    @Column(name = "p_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Ensures Profile ID is the same as Organisation ID
    @JoinColumn(name = "p_org_id")
    @JsonIgnore
    private Organisation organisation;

    @Column(name = "p_org_name")
    private String orgName;

    @Column(name = "p_org_reg_number")
    private String registrationNumber;

    @Column(name = "p_org_vat_number")
    private String vatNumber;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrgAddress address;

}