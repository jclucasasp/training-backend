-- ==========================================
-- 1. SUBSCRIPTION SYSTEM
-- ==========================================
DROP TABLE IF EXISTS subscription_plan;
CREATE TABLE subscription_plan (
                                   sup_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   sup_plan ENUM('MONTHLY', 'YEARLY'),
                                   sup_price DECIMAL(10, 2) NOT NULL,
                                   sup_course_limit INT DEFAULT 5,
                                   sup_is_active TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

-- Insert default Subscription plans
INSERT INTO subscription_plan (sup_plan, sup_price, sup_course_limit, sup_is_active) VALUES
('MONTHLY', 10.00, 5, 1),
('YEARLY', 100.00, 10, 1);

-- 1.1 Roles (Lookup Table)
DROP TABLE IF EXISTS role;
CREATE TABLE role (
    rol_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_name ENUM('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT'),
    rol_description VARCHAR(255)
) ENGINE=InnoDB;

-- Insert default roles
INSERT INTO role (rol_name, rol_description) VALUES
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
                              org_role_id TINYINT(1) DEFAULT 1,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                              ended_at DATETIME NULL,
                              -- Index for soft-delete filtering & auth lookups
                              INDEX idx_org_status (ended_at, org_id),
                              INDEX idx_org_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS organisation_subscription (
                                           osu_org_id BIGINT NOT NULL PRIMARY KEY ,
                                           osu_plan_id BIGINT NOT NULL,
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
                         pro_contact_number Integer,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_profile_org FOREIGN KEY (pro_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS address
(
    adr_org_id         BIGINT PRIMARY KEY, -- The Org ID is the same as the Profile ID
    adr_street         VARCHAR(255) NOT NULL,
    adr_suburb         VARCHAR(255),       -- Optional
    adr_city           VARCHAR(255) NOT NULL,
    adr_state          VARCHAR(255) NOT NULL,
    adr_zip            VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_address_profile FOREIGN KEY (adr_org_id) REFERENCES profile(pro_org_id)

) ENGINE=InnoDB;

-- 2.1 Staff (The 'Proxy' Users)
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

-- ==========================================
-- 3. AUTHENTICATION (API Keys)
-- ==========================================
CREATE TABLE IF NOT EXISTS api_key (
                         apk_org_id BIGINT PRIMARY KEY,
                         apk_prefix VARCHAR(12) NOT NULL, -- The first 12 characters of the API Key
                         apk_key_hash VARCHAR(255) UNIQUE NOT NULL, -- The hashed key for comparison
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_api_key_profile FOREIGN KEY (apk_org_id) REFERENCES organisation(org_id),
                         -- Fast lookup for auth filter
                         INDEX idx_api_auth (apk_key_hash, ended_at, apk_org_id),
                         INDEX idx_api_prefix (apk_prefix)
) ENGINE=InnoDB;

-- ==========================================
-- 4. COURSE HIERARCHY (Multi-Tenant)
-- ==========================================
CREATE TABLE IF NOT EXISTS course (
                        cou_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        cou_org_id BIGINT NOT NULL,
                        cou_stf_id BIGINT NOT NULL,
                        cou_name VARCHAR(255) NOT NULL,
                        cou_short_description TEXT,
                        cou_intended_audience TEXT,
                        cou_requirements TEXT,
                        cou_status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
                        cou_slug VARCHAR(255) NOT NULL,
                        cou_total_time_minutes INT,
                        cou_image_url VARCHAR(255), -- Optional: URL to course avatar image
                        cou_learning_objectives TEXT,
                        cou_difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
                        cou_tags VARCHAR(255),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        ended_at DATETIME NULL,
                        -- Ensure that other courses with the same slug are not created within the org
                        CONSTRAINT fk_course_slug UNIQUE (cou_id, cou_slug),
                        CONSTRAINT fk_course_org FOREIGN KEY (cou_org_id) REFERENCES organisation(org_id),
                        CONSTRAINT fk_course_staff FOREIGN KEY (cou_stf_id) REFERENCES staff(stf_id),
                        -- Index for tenant-specific course management
                        INDEX idx_course_tenant (cou_org_id, cou_id),
                        INDEX idx_course_creator (cou_stf_id, cou_id),
                        INDEX idx_course_status (ended_at, cou_id),
                        INDEX idx_course_slug (cou_slug, cou_org_id)
) ENGINE=InnoDB;

-- Enforcing 1 CourseModule per Course via UNIQUE constraint
CREATE TABLE IF NOT EXISTS chapter (
                        cha_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        cha_course_id BIGINT NOT NULL,
                        cha_name VARCHAR(255) NOT NULL,
                        cha_summary  TEXT,
                        cha_status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
                        cha_total_time_minutes INT,
                        cha_order_index INT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        ended_at DATETIME NULL,
                        CONSTRAINT fk_module_course FOREIGN KEY (cha_course_id) REFERENCES course(cou_id),
                        -- Index for Course -> CourseModule join
                        INDEX idx_chapter_order (cha_course_id, cha_order_index),
                        INDEX idx_module_course_fk (cha_course_id),
                        INDEX idx_module_status (ended_at, cha_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chapter_section (
                         chs_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         chs_chapter_id BIGINT NOT NULL,
                         chs_title VARCHAR(255) NOT NULL,
                         chs_content TEXT,
                         chs_is_preview TINYINT(1) DEFAULT 0,
                         chs_subtitles_url VARCHAR(255),
                         chs_duration_minutes INT,
                         chs_resource_url VARCHAR(255),
                         chs_resource_media_type VARCHAR(100),
                         chs_tags VARCHAR(255),
                         chs_order_index INT,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_chs_chapter FOREIGN KEY (chs_chapter_id) REFERENCES chapter(cha_id),
                         UNIQUE INDEX idx_unique_section_title (chs_chapter_id, chs_title)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attachment (
    att_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    att_chs_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_attachment_section FOREIGN KEY (att_chs_id) REFERENCES chapter_section(chs_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS quiz (
    quiz_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_org_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    quiz_passing_score INT,
    quiz_course_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    -- Ensure quiz title is unique within a specific course
    CONSTRAINT fk_quiz_course_ref FOREIGN KEY (quiz_course_id) REFERENCES course(cou_id),
    CONSTRAINT fk_quiz_org_ref FOREIGN KEY (quiz_org_id) REFERENCES organisation(org_id),
    UNIQUE INDEX idx_unique_quiz_title (quiz_course_id, title)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chapter_quizzes (
    cha_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    PRIMARY KEY (cha_id, quiz_id),
    CONSTRAINT fk_cq_chapter FOREIGN KEY (cha_id) REFERENCES chapter(cha_id),
    CONSTRAINT fk_cq_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
) ENGINE=InnoDB;

-- ==========================================
-- 5. STUDENTS (Tenant Bound)
-- ==========================================
CREATE TABLE IF NOT EXISTS student (
                         stu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         stu_org_id BIGINT NOT NULL,
                         stu_student_number VARCHAR(100) NOT NULL,
                         stu_first_name VARCHAR(100),
                         stu_last_name VARCHAR(100),
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_student_org FOREIGN KEY (stu_org_id) REFERENCES organisation(org_id),
                         -- Most common student lookup: "Does this student exist in THIS org?"
                         UNIQUE INDEX idx_student_tenant_auth (stu_org_id, stu_student_number)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_enrollment (
                                    ste_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    ste_student_id BIGINT NOT NULL,
                                    ste_course_id BIGINT NOT NULL,
                                    ste_current_section_id BIGINT NULL,
                                    ste_enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    ste_completed_at DATETIME NULL,
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                                    ended_at DATETIME NULL,
                                    CONSTRAINT fk_ste_student FOREIGN KEY (ste_student_id) REFERENCES student(stu_id),
                                    CONSTRAINT fk_ste_course FOREIGN KEY (ste_course_id) REFERENCES course(cou_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_progress (
    stp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stp_student_enrollment_id BIGINT NULL,
    stp_section_id BIGINT NULL,
    stp_percentage DECIMAL(5,2) DEFAULT 0.00,
    stp_is_completed BOOLEAN DEFAULT FALSE,
    stp_last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    stp_updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_stp_enrollment_ref FOREIGN KEY (stp_student_enrollment_id) REFERENCES student_enrollment(ste_id),
    CONSTRAINT fk_stp_section_ref FOREIGN KEY (stp_section_id) REFERENCES chapter_section(chs_id),
    UNIQUE INDEX idx_unique_stp (stp_student_enrollment_id, stp_section_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_quizzes (
    quiz_id BIGINT NOT NULL,
    stu_id BIGINT NOT NULL,
    PRIMARY KEY (quiz_id, stu_id),
    CONSTRAINT fk_sq_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id),
    CONSTRAINT fk_sq_student FOREIGN KEY (stu_id) REFERENCES student(stu_id)
) ENGINE=InnoDB;
