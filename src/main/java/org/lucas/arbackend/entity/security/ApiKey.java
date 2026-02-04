package org.lucas.arbackend.entity.security;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "api_key")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey extends BaseEntity {
    @Id
    @Column(name = "ak_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId // Ensures ApiKey ID is the same as Organisation ID
    @JoinColumn(name = "ak_org_id")
    private Organisation organisation;

    @Column(name = "ak_prefix", nullable = false, length = 12)
    private String prefix;

    @Column(name = "ak_key_hash", unique = true, nullable = false)
    private String hashKey;
}