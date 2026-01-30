package org.lucas.arbackend.entity.security;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "r_id")
    private Long id;

    @Column(name = "r_admin")
    private Boolean isAdmin;

    @Column(name = "r_editor")
    private Boolean isEditor;

    @Column(name = "r_user")
    private Boolean isUser;
}

