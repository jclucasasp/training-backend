package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.base.BaseEntity;

@Entity
@Table(name = "address")
@SQLDelete(sql = "UPDATE address SET ended_at = CURRENT_TIMESTAMP WHERE adr_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class OrgAddress extends BaseEntity {

    @Id
    @Column(name = "adr_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "adr_org_id")
    @JsonIgnore
    private Profile profile;

    @Column(name = "adr_street", nullable = false)
    private String street;

    @Column(name = "adr_suburb")
    private String suburb;

    @Column(name = "adr_city", nullable = false)
    private String city;

    @Column(name = "adr_state", nullable = false)
    private String state;

    @Column(name = "adr_zip", nullable = false)
    private String zip;
}
