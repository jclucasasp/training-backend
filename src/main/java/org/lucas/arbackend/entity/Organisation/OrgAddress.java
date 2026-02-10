package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;

@Entity
@Table(name = "address")
@SQLDelete(sql = "UPDATE address SET ended_at = CURRENT_TIMESTAMP WHERE a_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class OrgAddress extends BaseEntity {

    @Id
    @Column(name = "a_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "a_org_id")
    @JsonIgnore
    private Profile profile;

    @Column(name = "a_street", nullable = false)
    private String street;

    @Column(name = "a_suburb")
    private String suburb;

    @Column(name = "a_city", nullable = false)
    private String city;

    @Column(name = "a_state", nullable = false)
    private String state;

    @Column(name = "a_zip", nullable = false)
    private Integer zip;
}
