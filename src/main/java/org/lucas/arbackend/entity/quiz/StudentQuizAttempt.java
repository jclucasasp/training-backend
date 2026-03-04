package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_attempt")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentQuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sqa_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_org_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sqa_quiz_id", nullable = false)
    private Quiz quiz;

    private int score;
    private boolean isPassed;
    private LocalDateTime completedAt;
}
