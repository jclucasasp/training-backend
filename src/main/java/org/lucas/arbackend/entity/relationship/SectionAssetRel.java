package org.lucas.arbackend.entity.relationship;


import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.course.Asset;
import org.lucas.arbackend.entity.course.Section;

@Entity
@Table(name = "section_asset_rel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SectionAssetRel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sar_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sar_asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sar_section_id")
    private Section section;
}