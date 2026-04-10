package org.lucas.arbackend.entity.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;

@Entity
@Table(name = "api_key")
//@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey extends BaseEntity {
    @Id
    @Column(name = "apk_org_id")
    private Long orgId;

    @Column(name = "apk_prefix", length = 12)
    private String prefix;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId // Ensures ApiKey ID is the same as Organisation ID
    @JoinColumn(name = "apk_org_id")
    @JsonIgnore
    private Organisation organisation;

    @Column(name = "apk_key_hash", unique = true)
    private String hashKey;
}