package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "staff")
@SQLDelete(sql = "UPDATE staff SET ended_at = CURRENT_TIMESTAMP WHERE stf_id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class Staff extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stf_org_id")
    @JsonIgnore
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.EAGER) // Roles are usually small, EAGER is fine here
    @JoinColumn(name = "stf_role_id")
    private Role role;

    @Column(name = "stf_email")
    private String email;

    @Column(name = "stf_password")
    private String password;
}
