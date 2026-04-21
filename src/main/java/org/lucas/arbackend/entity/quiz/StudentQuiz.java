package org.lucas.arbackend.entity.quiz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.util.tenant.TenantEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentQuiz implements TenantEntity {
    @Id
    @Column(name = "sq_quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sq_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sq_quiz_id", nullable = false)
    @MapsId
    @JsonIgnore
    private Quiz quiz;

    @Column(name = "stq_assigned_at", updatable = false)
    LocalDateTime assignedAt = LocalDateTime.now();

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }
}
