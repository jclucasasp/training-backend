package org.lucas.arbackend.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.security.Role;

@Entity
@Table(name = "role_rel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleRel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rr_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rr_role_id")
    private Role role;
}