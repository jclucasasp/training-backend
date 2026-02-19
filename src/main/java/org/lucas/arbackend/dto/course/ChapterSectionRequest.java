package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ChapterSectionRequest {

    private Long id;

    @NotNull(message = "ChapterSectionRequest title is required")
    private String title;

    @NotNull(message = "ChapterSectionRequest content is required")
    private String content;

    @NotNull(message = "ChapterSectionRequest duration is required")
    private Integer duration;

    private String resourceUrl;
    private String resourceMediaType;

    @NotNull(message = "ChapterSectionRequest order index is required")
    private Integer orderIndex;
    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc")
    private String tags;

    private CourseChapterResponse chapterResponse;

}
