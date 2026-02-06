package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.Module;

import java.util.List;

@Data @Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private String imageUrl;
    private String tags;
//    private List<Module> modules;
    // We don't include modules in the list view to keep it light
}
