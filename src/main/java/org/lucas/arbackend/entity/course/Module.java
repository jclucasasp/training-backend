package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "module")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "m_id")
    private Long id;

    @Column(name = "m_name", nullable = false)
    private String name;

    @Column(name = "m_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "m_duration")
    private Integer duration;

    @CreatedDate
    @Column(name = "m_created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "m_updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "m_ended_at")
    private LocalDateTime endedAt;

    @Column(name = "m_tags", columnDefinition = "TEXT")
    private String tags;
}