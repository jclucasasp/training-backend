-- ==========================================
-- 1. SUBSCRIPTION & ROLES (Global Tables)
-- ==========================================
DROP TABLE IF EXISTS subscription_plan;
CREATE TABLE subscription_plan (
    sup_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sup_plan VARCHAR(150),
    sup_price DECIMAL(10, 2) NOT NULL,
    sup_course_limit INT DEFAULT 5,
    sup_is_active TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

INSERT INTO subscription_plan (sup_plan, sup_price, sup_course_limit, sup_is_active) VALUES
('MONTHLY', 10.00, 5, 1),
('YEARLY', 100.00, 10, 1);

DROP TABLE IF EXISTS role;
CREATE TABLE role (
    rol_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_name ENUM('INACTIVE', 'ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT'),
    rol_description VARCHAR(255)
) ENGINE=InnoDB;

INSERT INTO role (rol_name, rol_description) VALUES
('INACTIVE', 'No active subscription for the organisation'),
('ORG_ADMIN', 'Full control over the organisation'),
('COURSE_EDITOR', 'Can manage courses and courseModules but not billing'),
('SUPPORT', 'Can view student progress but not edit content'),
('STUDENT', 'Can view courses and update progress');


-- ==========================================
-- 2. TENANT CORE (Organisation & Profile)
-- ==========================================
CREATE TABLE IF NOT EXISTS organisation (
    org_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    org_password VARCHAR(255) NOT NULL,
    org_role_id BIGINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    INDEX idx_org_status (ended_at, org_id),
    INDEX idx_org_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS organisation_subscription (
    osu_org_id BIGINT NOT NULL PRIMARY KEY,
    osu_plan_id BIGINT NOT NULL,
    osu_subscription_amount DECIMAL(19,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    osu_status TINYINT(1) DEFAULT 1,
    CONSTRAINT fk_os_org FOREIGN KEY (osu_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_os_plan FOREIGN KEY (osu_plan_id) REFERENCES subscription_plan(sup_id),
    INDEX idx_os_org_active (osu_org_id, osu_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS profile (
    pro_org_id BIGINT PRIMARY KEY,
    pro_name VARCHAR(255) NOT NULL,
    pro_reg_number VARCHAR(100),
    pro_vat_number VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_profile_org FOREIGN KEY (pro_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS address (
    adr_org_id BIGINT PRIMARY KEY,
    adr_street VARCHAR(255) NOT NULL,
    adr_suburb VARCHAR(255),
    adr_city VARCHAR(255) NOT NULL,
    adr_state VARCHAR(255) NOT NULL,
    adr_zip VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_address_profile FOREIGN KEY (adr_org_id) REFERENCES profile(pro_org_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS staff (
    stf_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stf_org_id BIGINT NOT NULL,
    stf_role_id BIGINT NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    stf_password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_staff_org FOREIGN KEY (stf_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_staff_role FOREIGN KEY (stf_role_id) REFERENCES role(rol_id),
    UNIQUE INDEX idx_staff_email_org (email, stf_org_id)
) ENGINE=InnoDB;


-- ================================================
-- 3. AUTHENTICATION FOR API KEY AND PAYMENTS LOGS
-- ================================================
CREATE TABLE IF NOT EXISTS api_key (
    apk_org_id BIGINT PRIMARY KEY,
    apk_prefix VARCHAR(12) UNIQUE NULL,
    apk_key_hash VARCHAR(255) UNIQUE NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_api_key_profile FOREIGN KEY (apk_org_id) REFERENCES organisation(org_id),
    INDEX idx_api_auth (apk_key_hash, ended_at, apk_org_id),
    INDEX idx_api_prefix (apk_prefix)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment_logs (
    pal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pal_pf_payment_id VARCHAR(50) NOT NULL,
    pal_org_id BIGINT NOT NULL,
    pal_amount DECIMAL(19, 2) NOT NULL,
    pal_sub_cycles Integer DEFAULT 1,
    pal_plan_term VARCHAR(150) DEFAULT NULL,
    pal_billing_date TIMESTAMP DEFAULT NULL,
    pal_token VARCHAR(225) NULL,
    pal_payment_status VARCHAR(150) DEFAULT NULL,
    pal_sub_status VARCHAR(150) DEFAULT NULL,
    pal_failure_code VARCHAR(150) DEFAULT NULL,
    pal_failure_details TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIME ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP DEFAULT NULL,
    -- Crucial for Idempotency: Prevents duplicate processing at the DB level
    CONSTRAINT uk_pf_payment_id UNIQUE (pal_pf_payment_id),
    -- Index for reporting/audit lookups by organization
    INDEX idx_payment_org (pal_org_id),
    INDEX idx_pal_payment_status(pal_payment_status),
    INDEX idx_pal_failure_code(pal_failure_code)
) ENGINE=InnoDB;


-- ==========================================
-- 4. COURSE HIERARCHY (Multi-Tenant Denormalized)
-- ==========================================
CREATE TABLE IF NOT EXISTS course (
    cou_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cou_org_id BIGINT NOT NULL,
    cou_stf_id BIGINT NOT NULL,
    cou_name VARCHAR(255) NOT NULL,
    cou_short_description TEXT,
    cou_intended_audience TEXT,
    cou_requirements TEXT,
    cou_status VARCHAR(150) DEFAULT 'DRAFT',
    cou_slug VARCHAR(255) NOT NULL,
    cou_total_time_minutes INT,
    cou_image_url VARCHAR(255),
    cou_learning_objectives TEXT,
    cou_difficulty VARCHAR(150),
    cou_tags VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_course_org FOREIGN KEY (cou_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_course_staff FOREIGN KEY (cou_stf_id) REFERENCES staff(stf_id),
    UNIQUE INDEX idx_course_slug_org (cou_org_id, cou_slug),
    INDEX idx_course_tenant (cou_org_id, cou_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vr_scene(
    vr_sce_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_sce_org_id BIGINT NOT NULL,
    vr_sce_title VARCHAR(100) NOT NULL,
    vr_sce_description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_sce_org FOREIGN KEY (vr_sce_org_id) REFERENCES organisation(org_id),
    INDEX idx_vr_sce_org (vr_sce_org_id, vr_sce_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vr_scene_version(
    vr_sce_ver_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_sce_id BIGINT NOT NULL,
    vr_sce_version_tag VARCHAR(50) NOT NULL,
    vr_sce_change_log TEXT,
    vr_sce_is_active BOOLEAN DEFAULT FALSE,
    vr_sce_environmental_file_url VARCHAR(200),
    vr_sce_hierarchy_json LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_sce_ver_sce FOREIGN KEY (vr_sce_id) REFERENCES vr_scene(vr_sce_id),
    INDEX idx_vr_sce_ver_sce (vr_sce_id, vr_sce_ver_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chapter (
    cha_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cha_org_id BIGINT NOT NULL,
    cha_course_id BIGINT NOT NULL,
    cha_name VARCHAR(255) NOT NULL,
    cha_summary TEXT,
    cha_status VARCHAR(150) DEFAULT 'DRAFT',
    cha_total_time_minutes INT,
    cha_order_index INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_cha_org FOREIGN KEY (cha_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_cha_course FOREIGN KEY (cha_course_id) REFERENCES course(cou_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chapter_section (
    chs_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chs_org_id BIGINT NOT NULL,
    chs_chapter_id BIGINT NOT NULL,
    chs_vr_scene_id BIGINT NULL,
    chs_title VARCHAR(255) NOT NULL,
    chs_content TEXT,
    chs_is_preview TINYINT(1) DEFAULT 0,
    chs_subtitles_url VARCHAR(255),
    chs_duration_minutes INT,
    chs_resource_url VARCHAR(255),
    chs_resource_media_type VARCHAR(100),
    chs_scene_config LONGTEXT DEFAULT '{}',
    chs_tags VARCHAR(255),
    chs_order_index INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_chs_org FOREIGN KEY (chs_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_chs_chapter FOREIGN KEY (chs_chapter_id) REFERENCES chapter(cha_id),
    CONSTRAINT fk_chs_vr_scene FOREIGN KEY (chs_vr_scene_id) REFERENCES vr_scene(vr_sce_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attachment (
    att_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    att_org_id BIGINT NOT NULL,
    att_file_name VARCHAR(255) NOT NULL,
    att_file_type VARCHAR(255) NOT NULL,
    att_file_url VARCHAR(255) NOT NULL,
    att_chs_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_att_org FOREIGN KEY (att_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_att_section FOREIGN KEY (att_chs_id) REFERENCES chapter_section(chs_id)
) ENGINE=InnoDB;


-- ==========================================
-- 5. QUIZ SYSTEM (Fully Denormalized & Complete)
-- ==========================================
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
    CONSTRAINT fk_quiz_org_ref FOREIGN KEY (quiz_org_id) REFERENCES organisation(org_id),
    UNIQUE INDEX idx_unique_quiz_title (quiz_course_id, quiz_title)
) ENGINE=InnoDB;

-- NEW: Quiz Questions
CREATE TABLE IF NOT EXISTS quiz_question (
    qq_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qq_org_id BIGINT NOT NULL,
    qq_quiz_id BIGINT NOT NULL,
    qq_text TEXT NOT NULL,
    qq_type VARCHAR(150) DEFAULT 'MULTIPLE_CHOICE',
    qq_order_index INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_qq_org FOREIGN KEY (qq_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_qq_quiz FOREIGN KEY (qq_quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- NEW: Quiz Options
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

-- ==========================================
-- 6. STUDENTS & PROGRESS (Tenant Bound)
-- ==========================================
CREATE TABLE IF NOT EXISTS student (
    stu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stu_org_id BIGINT NOT NULL,
    stu_student_number VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    contact_number VARCHAR(100),
    stu_password VARCHAR(255),
    stu_role_id BIGINT(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_student_org FOREIGN KEY (stu_org_id) REFERENCES organisation(org_id),
    UNIQUE INDEX idx_student_tenant_auth (stu_org_id, stu_student_number)
) ENGINE=InnoDB;

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
-- This table tracks WHICH quizzes a student is ASSIGNED or ALLOWED to take
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

-- NEW: Tracks the actual ATTEMPT and SCORE of the quiz
CREATE TABLE IF NOT EXISTS student_quiz_attempt (
    sqa_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sqa_org_id BIGINT NOT NULL,
    sqa_student_id BIGINT NOT NULL,
    sqa_quiz_id BIGINT NOT NULL,
    sqa_score DECIMAL(5,2) NOT NULL,
    sqa_is_passed BOOLEAN DEFAULT FALSE,
    sqa_submitted_answers_json JSON,
    sqa_started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sqa_completed_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    CONSTRAINT fk_sqa_org FOREIGN KEY (sqa_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_sqa_student FOREIGN KEY (sqa_student_id) REFERENCES student(stu_id),
    CONSTRAINT fk_sqa_quiz FOREIGN KEY (sqa_quiz_id) REFERENCES quiz(quiz_id)
) ENGINE=InnoDB;
-- ==========================================
-- 7. COURSE Q&A / DISCUSSIONS
-- ==========================================

-- Table for the main question asked by a student
CREATE TABLE IF NOT EXISTS course_question (
    cq_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cq_org_id BIGINT NOT NULL,
    cq_course_id BIGINT NOT NULL,
    cq_section_id BIGINT NULL,
    cq_student_id BIGINT NOT NULL,
    cq_title VARCHAR(255) NOT NULL,
    cq_body TEXT NOT NULL,
    cq_is_resolved BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,

    CONSTRAINT fk_cq_org FOREIGN KEY (cq_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_cq_course FOREIGN KEY (cq_course_id) REFERENCES course(cou_id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_section FOREIGN KEY (cq_section_id) REFERENCES chapter_section(chs_id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_student FOREIGN KEY (cq_student_id) REFERENCES student(stu_id)
) ENGINE=InnoDB;

-- Table for replies from either Lecturers (Staff) or other Students
CREATE TABLE IF NOT EXISTS course_question_reply (
    cqr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cqr_org_id BIGINT NOT NULL,
    cqr_question_id BIGINT NOT NULL,
    cqr_student_id BIGINT NULL,
    cqr_staff_id BIGINT NULL,
    cqr_body TEXT NOT NULL,
    cqr_is_accepted_answer BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,

    CONSTRAINT fk_cqr_org FOREIGN KEY (cqr_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_cqr_question FOREIGN KEY (cqr_question_id) REFERENCES course_question(cq_id) ON DELETE CASCADE,
    CONSTRAINT fk_cqr_student FOREIGN KEY (cqr_student_id) REFERENCES student(stu_id),
    CONSTRAINT fk_cqr_staff FOREIGN KEY (cqr_staff_id) REFERENCES staff(stf_id),

    -- Ensure a reply has an author (either staff or student, but not neither)
    CONSTRAINT chk_reply_author CHECK (cqr_student_id IS NOT NULL OR cqr_staff_id IS NOT NULL)
) ENGINE=InnoDB;

-- ==========================================
-- 8. VR TRAINING TELEMETRY
-- ==========================================

CREATE TABLE IF NOT EXISTS vr_session (
    vr_ses_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_ses_org_id BIGINT NOT NULL,
    vr_ses_scene_version_id BIGINT NOT NULL,
    vr_ses_student_id BIGINT NOT NULL,
    vr_ses_section_id BIGINT NOT NULL,
    vr_ses_device_id VARCHAR(100),
    vr_ses_headset_model VARCHAR(50),
    vr_ses_started_at DATETIME NOT NULL,
    vr_ses_ended_at DATETIME NULL,
    vr_ses_duration_seconds INT,
    vr_ses_comfort_rating TINYINT,
    vr_ses_motion_sickness_reported BOOLEAN DEFAULT FALSE,
    vr_ses_session_quality_score DECIMAL(3,2),
    vr_ses_avg_fps DECIMAL(4,1),
    vr_ses_frame_drop_count INT DEFAULT 0,
    vr_ses_tracking_loss_count INT DEFAULT 0,
    vr_ses_interaction_count INT DEFAULT 0,
    vr_ses_hint_request_count INT DEFAULT 0,
    vr_ses_failure_count INT DEFAULT 0,
    vr_ses_completion_condition_met BOOLEAN DEFAULT FALSE,
    vr_ses_completion_time_ms BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_ses_org FOREIGN KEY (vr_ses_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_vr_ses_student FOREIGN KEY (vr_ses_student_id) REFERENCES student(stu_id),
    CONSTRAINT fk_vr_ses_section FOREIGN KEY (vr_ses_section_id) REFERENCES chapter_section(chs_id),
    CONSTRAINT fk_vr_ses_scene_version FOREIGN KEY (vr_ses_scene_version_id) REFERENCES vr_scene_version(vr_sce_ver_id),
    INDEX idx_vr_ses_student_time (vr_ses_student_id, vr_ses_started_at),
    INDEX idx_vr_ses_org_active (vr_ses_org_id, vr_ses_ended_at),
    INDEX idx_vr_ses_section (vr_ses_section_id, vr_ses_started_at),
    INDEX idx_vr_ses_scene_version (vr_ses_scene_version_id, vr_ses_started_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vr_event (
    vr_eve_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_eve_org_id BIGINT NOT NULL,
    vr_eve_session_id BIGINT NOT NULL,
    vr_eve_event_type VARCHAR(30) NOT NULL,
    vr_eve_timestamp DATETIME NOT NULL,
    vr_eve_position_x DECIMAL(10,4),
    vr_eve_position_y DECIMAL(10,4),
    vr_eve_position_z DECIMAL(10,4),
    vr_eve_rotation_x DECIMAL(10,4),
    vr_eve_rotation_y DECIMAL(10,4),
    vr_eve_rotation_z DECIMAL(10,4),
    vr_eve_target_object_id VARCHAR(100),
    vr_eve_duration_ms INT,
    vr_eve_metadata VARCHAR(2000),
    vr_eve_hand VARCHAR(10),
    vr_eve_sequence_number BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_eve_org FOREIGN KEY (vr_eve_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_vr_eve_session FOREIGN KEY (vr_eve_session_id) REFERENCES vr_session(vr_ses_id),
    INDEX idx_vr_eve_session_time (vr_eve_session_id, vr_eve_timestamp),
    INDEX idx_vr_eve_type_target (vr_eve_event_type, vr_eve_target_object_id),
    INDEX idx_vr_eve_org_time (vr_eve_org_id, vr_eve_timestamp)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vr_asset(
    vr_asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_asset_org_id BIGINT NOT NULL,
    vr_asset_name VARCHAR(100) NOT NULL,
    vr_asset_type VARCHAR(50) NOT NULL, -- ENUM('MODEL', 'TEXTURE', 'MATERIAL', 'ANIMATION', 'AUDIO', 'IMAGE', 'VIDEO', 'SCRIPT', 'OTHER')
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_asset_org FOREIGN KEY (vr_asset_org_id) REFERENCES organisation(org_id),
    INDEX idx_vr_asset_org (vr_asset_org_id, vr_asset_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vr_asset_variant(
    vr_asset_var_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vr_asset_id BIGINT NOT NULL,
    vr_asset_var_platform VARCHAR(50) NOT NULL, -- ENUM('STANDALONE_HEADSET', 'PC_VR', 'WEB_XR')
    vr_asset_var_lod_level Integer NOT NULL,
    vr_asset_var_url VARCHAR(100),
    vr_asset_var_file_size Integer NOT NULL,
    vr_asset_var_checksum TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_vr_asset_var_asset FOREIGN KEY (vr_asset_id) REFERENCES vr_asset(vr_asset_id),
    INDEX idx_vr_asset_var_asset (vr_asset_id, vr_asset_var_id)
) ENGINE=InnoDB;