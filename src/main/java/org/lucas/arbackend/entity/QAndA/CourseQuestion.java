package org.lucas.arbackend.entity.QAndA;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.util.TenantEntity;

@Entity
@Table(name = "course_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseQuestion extends BaseEntity implements TenantEntity {
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cq_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cq_org_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cq_course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cq_section_id")
    private ChapterSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cq_student_id", nullable = false)
    private Student student;

    private String title;
    private String body;
    private boolean isResolved;

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

}
