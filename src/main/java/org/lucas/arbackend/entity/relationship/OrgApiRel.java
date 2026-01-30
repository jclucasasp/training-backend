package org.lucas.arbackend.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.Organisation;

@Entity
@Table(name = "org_api_rel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class OrgApiRel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oar_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oar_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oar_key_id")
    private ApiKey apiKey;
}