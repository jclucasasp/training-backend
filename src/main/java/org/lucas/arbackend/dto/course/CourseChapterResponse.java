package org.lucas.arbackend.dto.course;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Builder
public record CourseChapterResponse(
        long id,
        String name,
        String summary,
        Integer totalTimeInMinutes,
        Set<ChapterSectionResponse> sectionsResponse

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
