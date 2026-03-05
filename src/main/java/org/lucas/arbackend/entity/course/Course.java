package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.course.misc.DifficultyTypes;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Course.withChapterAndSections",
        attributeNodes = {
                @NamedAttributeNode(value = "chapters", subgraph = "chapterSections")
},
subgraphs = {
        @NamedSubgraph(name = "chapterSections",
                attributeNodes = {
                        @NamedAttributeNode(value = "chapterSections", subgraph = "attachments")
                }),
        @NamedSubgraph(name = "attachments",
                attributeNodes = {
                        @NamedAttributeNode("attachments")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cou_org_id")
    private Organisation organisation;

    // TODO: Create an admin endpoint to reassign courses to staff
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cou_stf_id")
    private Staff staff;

    @Column(name = "cou_name", nullable = false)
    private String name;

    @Column(name = "cou_short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "cou_intended_audience", columnDefinition = "TEXT")
    private String intendedAudience;

    @Column(name = "cou_requirements", columnDefinition = "TEXT")
    private String requirements;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "cou_status", columnDefinition = "ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT'")
    private StatusTypes status = StatusTypes.DRAFT;

    @Column(name = "cou_slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "cou_total_time_minutes")
    private Integer totalTimeInMinutes;

    @Column(name = "cou_image_url")
    private String imageUrl;

    @Column(name = "cou_learning_objectives")
    private String learningObjectives;

    @Enumerated(EnumType.STRING)
    @Column(name = "cou_difficulty")
    private DifficultyTypes difficultyTypes;

    @Column(name = "cou_tags")
    private String tags;

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<Chapter> chapters = new HashSet<>();

    // Automatically generate a slug based on the fileName
    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.name != null) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
        }
    }
}