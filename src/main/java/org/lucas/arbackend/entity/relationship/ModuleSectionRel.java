package org.lucas.arbackend.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Module;
import org.lucas.arbackend.entity.course.Section;

@Entity
@Table(name = "Module_Section_Rel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModuleSectionRel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msr_module_id")
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msr_section_id")
    private Section section;
}