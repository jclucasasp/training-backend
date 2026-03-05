package org.lucas.arbackend.entity.quiz;

public enum QuizTypes {
    MULTIPLE_CHOICE, TRUE_FALSE;

    public static QuizTypes fromString(QuizTypes type) {
        return type;
    }
}
