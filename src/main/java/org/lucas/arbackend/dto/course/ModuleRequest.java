package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class ModuleRequest {

    @NotNull(message = "Module name is required")
    private String name;

    @NotNull(message = "Module description is required")
    private String description;

    @NotNull(message = "Module sections are required")
    private List<SectionRequest> sections;
}
