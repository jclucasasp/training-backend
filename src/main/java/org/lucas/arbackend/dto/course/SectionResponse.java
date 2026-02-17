package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data @Builder
public class SectionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private Integer duration;
    private String resourceUrl;
    private String resourceMediaType;
    private String tags;
}
