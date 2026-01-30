package org.lucas.arbackend.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Course;
import org.lucas.arbackend.entity.course.Module;

@Entity
@Table(name = "course_module_rel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModuleRel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cmr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmr_course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmr_module_id")
    private Module module;
}