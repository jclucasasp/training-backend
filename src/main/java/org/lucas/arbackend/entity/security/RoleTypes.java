package org.lucas.arbackend.entity.security;

public enum RoleTypes {
    INACTIVE,
    ORG_ADMIN ,
    COURSE_EDITOR,
    SUPPORT,
    STUDENT;

    public static RoleTypes fromString(RoleTypes role) {
        return role;
    }
}
