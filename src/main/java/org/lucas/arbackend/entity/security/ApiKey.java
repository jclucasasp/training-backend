package org.lucas.arbackend.entity.security;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "api_key")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ak_id")
    private Long id;

    @Column(name = "ak_value", unique = true, nullable = false)
    private String value;
}