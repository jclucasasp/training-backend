package org.lucas.arbackend.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.quiz.Quiz;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.util.TenantEntity;

@Entity
@Table(name = "student_quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentQuiz implements TenantEntity {
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sq_org_id") // Organisation for multi-tenancy
    private Organisation organisation;

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }
}
