package org.lucas.arbackend.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.Organisation;

@Entity
@Table(name = "Org_Course_Rel")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrgCourseRel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ocr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_course_id")
    private Course course;
}