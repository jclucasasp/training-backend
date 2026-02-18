package org.lucas.arbackend.dto.course;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Builder
public record CourseResponse(
        Long id,
        String name,
        String description,
        String difficulty,
        String imageUrl,
        String tags,
        Set<CourseChapterResponse> chapterResponses
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


}
