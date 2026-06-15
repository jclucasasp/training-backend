package org.lucas.arbackend.entity.student;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.quiz.StudentQuiz;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.util.tenant.TenantEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student", uniqueConstraints = {
        @UniqueConstraint(columnNames ={"stu_org_id", "stu_student_number"})
})
@SQLDelete(sql = "UPDATE student SET ended_at = CURRENT_TIMESTAMP WHERE stu_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student extends BaseEntity implements TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "stu_student_number", nullable = false)
    private String studentNumber;

    @Column(name = "stu_first_name", nullable = true)
    private String firstName;

    @Column(name = "stu_last_name", nullable = true)
    private String lastName;

    @Column(name = "stu_email", nullable = true)
    @Email
    private String email;

    @Column(name = "stu_password", nullable = true)
    private String password;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stu_role_id")
    private Role role;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentEnrollment> studentEnrollment = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentQuiz> studentQuizzes = new HashSet<>();

    @Override
    public Organisation getOrganisation() {
        return this.organisation;
    }

    @Override
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

}