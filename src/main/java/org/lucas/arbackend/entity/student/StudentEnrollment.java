package org.lucas.arbackend.entity.student;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Course;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_enrollment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEnrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "se_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "se_student_number", referencedColumnName = "st_student_number")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "se_course_id")
    private Course course;

    @CreatedDate
    @Column(name = "se_enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "se_completed_at")
    private LocalDateTime completedAt;
}