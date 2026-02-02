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

    @Column(name = "r_name")
    private String name;

    @Column(name = "r_description")
    private String description;
}

