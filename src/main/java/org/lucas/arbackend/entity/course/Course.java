package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_id")
    private Long id;

    @Column(name = "c_name", nullable = false)
    private String name;

    @Column(name = "c_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "c_difficulty")
    private String difficulty;

    @CreatedDate
    @Column(name = "c_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "c_ended_at")
    private LocalDateTime endedAt;
}