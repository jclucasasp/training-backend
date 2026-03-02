package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
public class CourseChapterRequest {

    @JsonIgnore
    private Long id;

    @NotNull(message = "Chapter name is required", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotNull(message = "Chapter summary is required", groups = ValidatedLabel.OnCreate.class)
    private String summary;

    @NotNull(message = "Chapter sections are required", groups = ValidatedLabel.OnCreate.class)
    private List<ChapterSectionRequest> sections;
}
