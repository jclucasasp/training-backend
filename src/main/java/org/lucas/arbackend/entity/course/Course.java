package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Course.withChapterAndSections",
        attributeNodes = {
        @NamedAttributeNode("chapters"),
        @NamedAttributeNode(value = "chapters", subgraph = "chapterSections")
},
subgraphs = { @NamedSubgraph(name = "chapterSections",
        attributeNodes = {
        @NamedAttributeNode("chapterSections")
})
})
@Table(name = "course")
@SQLDelete(sql = "UPDATE course SET ended_at = CURRENT_TIMESTAMP WHERE cou_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cou_id")
    private Long id;

    @Column(name = "cou_name", nullable = false)
    private String name;

    @Column(name = "cou_image_url")
    private String imageUrl;

    @Column(name = "cou_description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "cou_difficulty")
    private DifficultyTypes difficultyTypes;

    @Column(name = "cou_tags")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cou_org_id")
    private Organisation organisation;

    // TODO: Create an admin endpoint to reassign courses to staff
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cou_stf_id")
    private Staff staff;

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Chapter> chapters = new HashSet<>();
}