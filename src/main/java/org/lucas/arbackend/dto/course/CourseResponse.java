package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.Module;

import java.util.List;
import java.util.Set;

@Data @Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private String imageUrl;
    private String tags;
    private Set<ModuleResponse> modules;
}
