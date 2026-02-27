package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ChapterSectionRequest {

    @JsonIgnore
    private Long id;

    @NotNull(message = "ChapterSectionRequest title is required")
    private String title;

    @NotNull(message = "ChapterSectionRequest content is required")
    private String content;

    @NotNull(message = "ChapterSectionRequest duration is required")
    private Integer durationInMinutes;

    private String resourceUrl;
    private String resourceMediaType;

    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc")
    private String tags;

}
