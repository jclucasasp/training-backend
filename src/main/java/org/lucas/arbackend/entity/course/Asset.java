package org.lucas.arbackend.entity.course;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_course_id")
    private Course course;

    @Column(name = "a_file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "a_file_type")
    private String fileType;
}