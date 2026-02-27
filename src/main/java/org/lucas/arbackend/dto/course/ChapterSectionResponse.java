package org.lucas.arbackend.dto.course;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

@Builder
public record ChapterSectionResponse (
        long id,
        String title,
        Integer durationInMinutes,
        String resourceUrl,
        String resourceMediaType,
        String tags
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
