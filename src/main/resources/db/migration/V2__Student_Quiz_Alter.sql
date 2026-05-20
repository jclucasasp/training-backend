SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS quiz;
CREATE TABLE quiz (
    quiz_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_org_id BIGINT NOT NULL,
    quiz_course_id BIGINT NOT NULL,
    quiz_title VARCHAR(255) NOT NULL,
    quiz_passing_score INT,
    quiz_max_attempts INT DEFAULT 3,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_quiz_course_ref FOREIGN KEY (quiz_course_id) REFERENCES course(cou_id),
    CONSTRAINT fk_quiz_org_ref FOREIGN KEY (quiz_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS chapter_quizzes;
CREATE TABLE chapter_quizzes (
    cq_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cha_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    quiz_org_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    CONSTRAINT fk_cq_chapter FOREIGN KEY (cha_id) REFERENCES chapter(cha_id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE,
    INDEX idx_cq_lookup (cha_id, quiz_id)
) ENGINE=InnoDB;

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

DROP TABLE IF EXISTS quiz_question_option;
CREATE TABLE IF NOT EXISTS quiz_question_option (
    qto_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qto_org_id BIGINT NOT NULL,
    qto_question_id BIGINT NOT NULL,
    qto_text TEXT NOT NULL,
    qto_is_correct BOOLEAN DEFAULT FALSE,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    CONSTRAINT fk_qto_org FOREIGN KEY (qto_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_qto_question FOREIGN KEY (qto_question_id) REFERENCES quiz_question(qq_id) ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS student_enrollment;
CREATE TABLE IF NOT EXISTS student_enrollment (
 ste_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ste_org_id BIGINT NOT NULL,
    ste_student_id BIGINT NOT NULL,
    ste_course_id BIGINT NOT NULL,
    ste_total_progress DECIMAL(5,2) DEFAULT 0.00,
    ste_enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ste_completed_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_ste_org FOREIGN KEY (ste_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_ste_student FOREIGN KEY (ste_student_id) REFERENCES student(stu_id),
    CONSTRAINT fk_ste_course FOREIGN KEY (ste_course_id) REFERENCES course(cou_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS student_progress;
CREATE TABLE IF NOT EXISTS student_progress (
    stp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stp_org_id BIGINT NOT NULL,
    stp_student_enrollment_id BIGINT NOT NULL,
    stp_chapter_id BIGINT NOT NULL,
    stp_section_id BIGINT NOT NULL,
    stp_percentage DECIMAL(5,2) DEFAULT 0.00,
    stp_is_completed BOOLEAN DEFAULT FALSE,
    stp_last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_stp_org FOREIGN KEY (stp_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_stp_enrollment_ref FOREIGN KEY (stp_student_enrollment_id) REFERENCES student_enrollment(ste_id),
    CONSTRAINT fk_stp_chapter_ref FOREIGN KEY (stp_chapter_id) REFERENCES chapter(cha_id),
    CONSTRAINT fk_stp_section_ref FOREIGN KEY (stp_section_id) REFERENCES chapter_section(chs_id),
    UNIQUE INDEX idx_unique_stp (stp_student_enrollment_id, stp_section_id)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;