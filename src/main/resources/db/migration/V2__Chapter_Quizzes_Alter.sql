-- Drop it so we start from a clean slate
DROP TABLE IF EXISTS chapter_quizzes;

CREATE TABLE chapter_quizzes (
    cq_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cha_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    quiz_org_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    CONSTRAINT fk_cq_chapter FOREIGN KEY (cha_id) REFERENCES chapter(cha_id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE,
    INDEX idx_cq_lookup (cha_id, quiz_id)
) ENGINE=InnoDB;