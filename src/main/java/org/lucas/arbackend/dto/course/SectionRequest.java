package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SectionRequest {

    @NotNull(message = "Section title is required")
    private String title;

    @NotNull(message = "Section content is required")
    private String content;

    @NotNull(message = "Section duration is required")
    private Integer duration;

    @NotNull(message = "Section order index is required")
    private Integer orderIndex;

    private String resourceUrl;
    private String resourceMediaType;

    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc")
    private String tags;
}
