package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data @Builder
public class CourseChapterResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String summary;

    private Set<ChapterSectionResponse> sectionsResponse;
}
