package org.lucas.arbackend.dto.course;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
public record CourseResponse(
        Long id,
        String name,
        Integer totalTimeInMinutes,
        String slug,
        String learningObjectives,
        String staffEmail,
        String difficulty,
        String imageUrl,
        String tags,
        List<CourseChapterResponse> chaptersResponse
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


}
