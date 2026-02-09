package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.security.ApiKey;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "profile")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile extends BaseEntity {
    @Id
    @Column(name = "p_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Ensures Profile ID is the same as Organisation ID
    @JoinColumn(name = "p_org_id")
//    @JsonBackReference // Prevents infinite recursion when serializing
    private Organisation organisation;

    @Column(name = "p_org_name")
    private String orgName;

    @Column(name = "p_org_reg_number")
    private String registrationNumber;

    @Column(name = "p_org_vat_number")
    private String vatNumber;

    @OneToOne(mappedBy = "profile", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private ApiKey apiKey;
}