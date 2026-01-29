package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Section")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_id")
    private Long id;

    @Column(name = "s_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "s_duration")
    private Integer duration;

    @CreatedDate
    @Column(name = "s_created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "s_updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "s_ended_at")
    private LocalDateTime endedAt;

    @Column(name = "s_tags", columnDefinition = "TEXT")
    private String tags;
}