package org.lucas.arbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class CacheDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String roleName;
    private Long orgId;
}
