package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Course.withModulesAndSections",
        attributeNodes = {
        @NamedAttributeNode("modules"),
        @NamedAttributeNode(value = "modules", subgraph = "sections")
},
subgraphs = { @NamedSubgraph(name = "sections",
        attributeNodes = {
        @NamedAttributeNode("sections")
})
})
@Table(name = "course")
@EntityListeners(AuditingEntityListener.class)
//@SQLRestriction("ended_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_id")
    private Long id;

    @Column(name = "c_name", nullable = false)
    private String name;

    @Column(name = "c_image_url")
    private String imageUrl;

    @Column(name = "c_description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "c_difficulty")
    private DifficultyTypes difficultyTypes;

    @Column(name = "c_tags")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_org_id")
    private Organisation organisation;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<Module> modules = new HashSet<>();
}