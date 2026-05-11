DROP TABLE IF EXISTS student_quizzes;

CREATE TABLE student_quizzes (
    stq_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stq_org_id BIGINT NOT NULL,
    stq_quiz_id BIGINT NOT NULL,
    stq_student_id BIGINT NOT NULL,
    stq_assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    CONSTRAINT fk_sq_org FOREIGN KEY (stq_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_sq_quiz FOREIGN KEY (stq_quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE,
    CONSTRAINT fk_sq_student FOREIGN KEY (stq_student_id) REFERENCES student(stu_id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_quiz (stq_student_id, stq_quiz_id) -- Prevents double registration
) ENGINE=InnoDB;