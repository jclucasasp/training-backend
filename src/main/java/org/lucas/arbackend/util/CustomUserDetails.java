package org.lucas.arbackend.util;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
public class CustomUserDetails extends User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long orgId;
    private final String email;
    private final String roleName;

    public CustomUserDetails(String email, String password, Long orgId, String roleName) {
        super(email, password, List.of(new SimpleGrantedAuthority(roleName)));
        this.orgId = orgId;
        this.email = email;
        this.roleName = roleName;
    }
}
