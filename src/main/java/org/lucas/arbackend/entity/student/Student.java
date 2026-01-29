package org.lucas.arbackend.entity.student;


import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.Organisation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Student")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "st_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "st_org_id")
    private Organisation organisation;

    @Column(name = "st_student_number", unique = true, nullable = false)
    private String studentNumber;

    @CreatedDate
    @Column(name = "st_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "st_ended_at")
    private LocalDateTime endedAt;
}