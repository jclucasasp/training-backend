package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data @Builder
public class CourseChapterRequest {

    @JsonIgnore
    private Long id;

    @NotNull(message = "CourseModule name is required")
    private String name;

    @NotNull(message = "CourseModule description is required")
    private String description;

    @NotNull(message = "CourseModule chapterSections are required")
    private Set<ChapterSectionRequest> sections;
}
