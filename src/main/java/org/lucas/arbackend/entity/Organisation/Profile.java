package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "profile")
@SQLDelete(sql = "UPDATE profile SET ended_at = CURRENT_TIMESTAMP WHERE pro_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile extends BaseEntity {
    @Id
    @Column(name = "pro_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Ensures Profile ID is the same as Organisation ID
    @JoinColumn(name = "pro_org_id")
    @JsonIgnore
    private Organisation organisation;

    @Column(name = "pro_name")
    private String orgName;

    @Column(name = "pro_reg_number")
    private String registrationNumber;

    @Column(name = "pro_vat_number")
    private String vatNumber;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrgAddress address;

}