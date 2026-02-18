package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Course.withModulesAndSections",
        attributeNodes = {
        @NamedAttributeNode("courseModules"),
        @NamedAttributeNode(value = "courseModules", subgraph = "chapterSections")
},
subgraphs = { @NamedSubgraph(name = "chapterSections",
        attributeNodes = {
        @NamedAttributeNode("chapterSections")
})
})
@Table(name = "course")
@SQLDelete(sql = "UPDATE course SET ended_at = CURRENT_TIMESTAMP WHERE c_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
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

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseChapter> courseChapters = new HashSet<>();
}