package org.lucas.arbackend.dto.course;

import lombok.Builder;
import org.lucas.arbackend.dto.quiz.QuizResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
public record CourseChapterResponse(
        long id,
        String name,
        String summary,
        String status,
        Integer totalTimeInMinutes,
        List<ChapterSectionResponse> sections,
        List<QuizResponse> chapterQuizzes

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
