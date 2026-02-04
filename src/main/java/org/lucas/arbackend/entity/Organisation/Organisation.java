package org.lucas.arbackend.entity.Organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "organisation")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Organisation extends BaseEntity {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private Long id;

    @Column(name = "org_email", unique = true, nullable = false)
    private String email;

    @Column(name = "org_password", nullable = false)
    private String password;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL)
    @JsonIgnore
    private ApiKey apiKey;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "org_role_id")
    private Role role;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL)
    private Profile profile;
}
