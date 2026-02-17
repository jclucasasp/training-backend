package org.lucas.arbackend.dto.organisation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.AccessLevelViews;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data @Builder
public class StaffResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonView(AccessLevelViews.Public.class)
    private Long id;
    @JsonView(AccessLevelViews.Public.class)
    private String firstName;
    @JsonView(AccessLevelViews.Public.class)
    private String lastName;
    @JsonView(AccessLevelViews.Public.class)
    private String email;
    @JsonView(AccessLevelViews.Public.class)
    private String contactNumber;
    @JsonView(AccessLevelViews.Public.class)
    private String role;
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime createdAt;
    @JsonView(AccessLevelViews.Public.class)
    private LocalDateTime updatedAt;

    @JsonView(AccessLevelViews.Internal.class)
    private LocalDateTime endedAt;
}
