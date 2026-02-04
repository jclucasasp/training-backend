package org.lucas.arbackend.util;

import lombok.Getter;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class StaffUserDetails extends User {
    private final Long orgId;

    public StaffUserDetails(String email, String password, Long orgId, String roleName) {
        super(email, password, List.of(new SimpleGrantedAuthority(roleName)));
        this.orgId = orgId;
    }
}
