package org.lucas.arbackend.entity.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "api_key")
// TODO: Remove the sqldelete for all entities that uses the orgId as their main id as it will not work and the endedAt need to be set manually, or create a softDelete method in the repo with a custom sql
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
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