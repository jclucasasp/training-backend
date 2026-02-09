package org.lucas.arbackend.dto.course;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data @Builder
public class ModuleResponse {

    private long id;
    private String name;
    private String description;

    private Set<SectionResponse> sections;
}
