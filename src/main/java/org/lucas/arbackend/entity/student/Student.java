package org.lucas.arbackend.entity.student;


import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "student")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "st_id")
    private Long id;

    @Column(name = "st_name", nullable = true)
    private String name;

    @Column(name = "st_last_name", nullable = true)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "st_org_id")
    private Organisation organisation;

    @Column(name = "st_student_number", nullable = false)
    private String studentNumber;

}