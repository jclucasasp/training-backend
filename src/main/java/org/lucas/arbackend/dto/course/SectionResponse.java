package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SectionResponse {

    private long id;
    private String title;
    private Integer duration;
    private String resourceUrl;
    private String resourceMediaType;
    private String tags;
}
