package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.dto.course.attachment.AttachmentRequest;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
public class ChapterSectionRequest {

    @JsonIgnore
    private Long id;

    @NotNull(message = "ChapterSectionRequest title is required", groups = ValidatedLabel.OnCreate.class)
    private String title;

    @NotNull(message = "ChapterSectionRequest content is required", groups = ValidatedLabel.OnCreate.class)
    private String content;

    @NotNull(message = "ChapterSectionRequest duration is required", groups = ValidatedLabel.OnCreate.class)
    private Integer durationInMinutes;

    private String resourceUrl;
    private String resourceMediaType;
    private String subtitlesUrl;
    private boolean isPreview;
    private List<AttachmentRequest> attachments;

    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc", groups = ValidatedLabel.OnCreate.class)
    private String tags;

}
