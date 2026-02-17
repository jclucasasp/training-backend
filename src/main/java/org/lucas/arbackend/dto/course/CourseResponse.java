package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data @Builder
public class CourseResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private String imageUrl;
    private String tags;
    private Set<ModuleResponse> modules;
}
