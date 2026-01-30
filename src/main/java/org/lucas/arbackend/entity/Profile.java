package org.lucas.arbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Profile")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {
    @Id
    @Column(name = "p_org_id")
    private Long orgId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Ensures Profile ID is the same as Organisation ID
    @JoinColumn(name = "p_org_id")
    private Organisation organisation;

    @Column(name = "p_org_name")
    private String orgName;

    @Column(name = "p_org_reg_number")
    private String registrationNumber;

    @Column(name = "p_org_vat_number")
    private String vatNumber;

    @LastModifiedDate
    @Column(name = "p_org_updated_at")
    private LocalDateTime updatedAt;
}