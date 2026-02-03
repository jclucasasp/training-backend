package org.lucas.arbackend.entity.security;

public enum RoleTypes {
    ORG_ADMIN ,
    COURSE_EDITOR,
    SUPPORT;

    public static RoleTypes fromString(RoleTypes role) {
        return role;
    }
}
