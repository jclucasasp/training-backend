package org.lucas.arbackend.entity.vr.competency;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@SQLDelete(sql = "UPDATE competencies SET ended_at = NOW() WHERE comp_id = ?")
@SQLRestriction("ended_at IS NULL")
@Table(name = "competencies")
public class Competency extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "comp_name", nullable = false)
    private String name;

    @Column(name = "comp_description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "comp_associated_scene_id")
    private String associatedSceneId;

    @OneToMany(mappedBy = "competency", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompetencyCriterion> criteria = new ArrayList<>();
}
