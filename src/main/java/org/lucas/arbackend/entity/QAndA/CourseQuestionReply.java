package org.lucas.arbackend.entity.QAndA;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.util.tenant.TenantEntity;

@Entity
@Table(name = "course_question_reply")
@SQLDelete(sql = "UPDATE course_question_reply SET ended_at = CURRENT_TIMESTAMP WHERE cpr_id = ?")
@SQLRestriction("ended_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseQuestionReply extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cqr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cqr_org_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cqr_question_id", nullable = false)
    private CourseQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cqr_student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cqr_staff_id")
    private Staff staff;

    private String body;
    private boolean isAcceptedAnswer;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
