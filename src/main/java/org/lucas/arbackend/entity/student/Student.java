package org.lucas.arbackend.entity.student;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "student", uniqueConstraints = {
        @UniqueConstraint(columnNames ={"stu_org_id", "stu_student_number"})
})
@SQLDelete(sql = "UPDATE student SET ended_at = CURRENT_TIMESTAMP WHERE stu_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stu_id")
    private Long id;

    @Column(name = "stu_first_name", nullable = true)
    private String firstName;

    @Column(name = "stu_last_name", nullable = true)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "stu_student_number", nullable = false)
    private String studentNumber;

}