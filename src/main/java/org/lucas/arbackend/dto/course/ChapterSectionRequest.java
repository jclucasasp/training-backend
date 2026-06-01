package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Section title missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The title of the section", example = "Section 1: Introduction to Subnets", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Section content missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The markdown content, description, or transcript for the chapter", example = "In this chapter we cover VPC structures...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @NotNull(message = "Section durationInMinutes missing or null", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The duration of the chapter in minutes", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationInMinutes;

    @Schema(description = "The URL of the resource, eg video, audio, etc", example = "https://www.youtube.com/watch?v=7e6QZ8o8Z2c")
    private String resourceUrl;

    @Schema(description = "The media type of the resource, eg video, audio, etc", example = "video/mp4", allowableValues = {"video", "document", "audio", "text"})
    private String resourceMediaType;

    @Schema(description = "Optional URL mapping for VTT or SRT closed captions", example = "https://www.resource.com/captions.vtt")
    private String subtitlesUrl;

    @Schema(description = "Flags whether unauthenticated users can access this section as a free preview", example = "false")
    private boolean isPreview;

    @Schema(description = "List of supplementary files or downloadable assets attached to this section")
    private List<AttachmentRequest> attachments;

    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Keywords for faster searching, eg Javascript, Anatomy, etc", example = "Javascript, Anatomy, etc", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tags;

}
