package org.lucas.arbackend.entity.course;

import lombok.Getter;

@Getter
public enum Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    public static Difficulty valueOf(Difficulty difficulty) {
        return difficulty;
    }
}
