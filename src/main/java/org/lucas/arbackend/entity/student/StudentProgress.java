package org.lucas.arbackend.entity.student;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Module;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Student_Progress")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sp_student_enrollment_id")
    private StudentEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sp_module_id")
    private Module module;

    @Column(name = "sp_percentage")
    private Double percentage;

    @LastModifiedDate
    @Column(name = "sp_updated_at")
    private LocalDateTime updatedAt;
}