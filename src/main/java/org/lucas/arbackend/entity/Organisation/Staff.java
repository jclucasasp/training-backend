package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.lucas.arbackend.entity.base.ContactBaseEntity;
import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "staff")
@SQLDelete(sql = "UPDATE staff SET ended_at = CURRENT_TIMESTAMP WHERE stf_id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class Staff extends ContactBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stf_org_id")
//    @JsonIgnore
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stf_role_id")
    private Role role;

    @JsonIgnore
    @Column(name = "stf_password")
    private String password;
}
