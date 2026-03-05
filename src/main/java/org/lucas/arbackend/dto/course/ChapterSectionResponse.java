package org.lucas.arbackend.dto.course;

import lombok.Builder;
import org.lucas.arbackend.dto.course.attachment.AttachmentResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
public record ChapterSectionResponse (
        long id,
        String title,
        String content,
        Integer durationInMinutes,
        String resourceUrl,
        String resourceMediaType,
        String subtitlesUrl,
        boolean isPreview,
        String tags,
        List<AttachmentResponse> attachments
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
