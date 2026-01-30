package org.lucas.arbackend.entity.security;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_key")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ak_id")
    private Long id;

    @Column(name = "ak_value", unique = true, nullable = false)
    private String value;

    @CreatedDate
    @Column(name = "ak_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ak_ended_at")
    private LocalDateTime endedAt;
}