package org.lucas.arbackend.entity.security;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_name", columnDefinition = "ENUM('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    private RoleTypes roleName;

    @Column(name = "rol_description")
    private String description;
}

