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

    @NotNull(message = "Chapter name is required")
    private String name;

    @NotNull(message = "Chapter summary is required")
    private String summary;

    @NotNull(message = "Chapter sections are required")
    private Set<ChapterSectionRequest> sections;
}
