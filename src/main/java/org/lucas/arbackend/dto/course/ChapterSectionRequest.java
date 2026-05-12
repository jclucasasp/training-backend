package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @NotBlank(message = "Param 'title' for chapter missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String title;

    @NotBlank(message = "Param 'content' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String content;

    @NotNull(message = "Param 'durationInMinutes' missing or null", groups = ValidatedLabel.OnCreate.class)
    private Integer durationInMinutes;

    private String resourceUrl;
    private String resourceMediaType;
    private String subtitlesUrl;
    private boolean isPreview;
    private List<AttachmentRequest> attachments;

    @NotNull(message = "Keywords for faster searching, eg Javascript, Anatomy, etc", groups = ValidatedLabel.OnCreate.class)
    private String tags;

}
