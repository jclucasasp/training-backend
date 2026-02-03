package org.lucas.arbackend.entity.Organisation;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "staff")
@SQLRestriction("stf_endded_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class Staff extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stf_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.EAGER) // Roles are usually small, EAGER is fine here
    @JoinColumn(name = "stf_role_id")
    private Role role;

    @Column(name = "stf_email")
    private String email;

    @Column(name = "stf_password")
    private String password;

    @Column(name = "stf_is_active")
    private boolean isActive = true;
}
