package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
public class CourseChapterRequest {

    @JsonIgnore
    private Long id;

    @NotNull(message = "Param 'name' for the chapter missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotNull(message = "Param 'summary' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String summary;

    @NotNull(message = "Param 'status' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private StatusTypes status;

    @NotEmpty(message = "List 'sections' missing or empty", groups = ValidatedLabel.OnCreate.class)
    private List<ChapterSectionRequest> sections;

    private List<Long> quizIds;
}
