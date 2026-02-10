package org.lucas.arbackend.entity.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "api_key")
@SQLDelete(sql = "UPDATE api_key SET ended_at = CURRENT_TIMESTAMP WHERE ak_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey extends BaseEntity {
    @Id
    @Column(name = "ak_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId // Ensures ApiKey ID is the same as Organisation ID
    @JoinColumn(name = "ak_org_id")
    @JsonIgnore
    private Profile profile;

    @Column(name = "ak_prefix", nullable = false, length = 12)
    private String prefix;

    @Column(name = "ak_key_hash", unique = true, nullable = false)
    private String hashKey;
}