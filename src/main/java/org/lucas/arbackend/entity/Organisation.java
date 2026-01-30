package org.lucas.arbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "organisation")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Organisation {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private Long id;

    @Column(name = "org_email", unique = true, nullable = false)
    private String email;

    @Column(name = "org_password", nullable = false)
    private String password;

    @LastModifiedDate
    @Column(name = "org_updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "org_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "org_ended_at")
    private LocalDateTime endedAt;

    @LastModifiedDate
    @Column(name = "org_password_reset_date")
    private LocalDateTime passwordResetDate;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL)
    Profile profile;
}
