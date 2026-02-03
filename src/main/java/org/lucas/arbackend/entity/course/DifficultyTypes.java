package org.lucas.arbackend.entity.course;

import lombok.Getter;

@Getter
public enum DifficultyTypes {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    public static DifficultyTypes valueOf(DifficultyTypes difficultyTypes) {
        return difficultyTypes;
    }
}
