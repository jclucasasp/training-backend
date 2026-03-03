package org.lucas.arbackend.entity.course.misc;

import lombok.Getter;

@Getter
public enum StatusTypes {
    DRAFT, PUBLISHED, ARCHIVED;

    public static StatusTypes valueOff(StatusTypes statusType) {
        return statusType;
    }
}


