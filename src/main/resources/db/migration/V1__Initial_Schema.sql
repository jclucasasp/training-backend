-- ==========================================
-- 1. SUBSCRIPTION SYSTEM
-- ==========================================
CREATE TABLE subscription_plan (
                                   sp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   sp_plan ENUM('MONTHLY', 'YEARLY'),
                                   sp_price DECIMAL(10, 2) NOT NULL,
                                   sp_course_limit INT DEFAULT 5,
                                   sp_is_active TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

-- Insert default Subscription plans
INSERT INTO subscription_plan (sp_plan, sp_price, sp_course_limit, sp_is_active) VALUES
('MONTHLY', 10.00, 5, 1),
('YEARLY', 100.00, 10, 1);

-- 1.1 Roles (Lookup Table)
CREATE TABLE role (
    r_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    r_name ENUM('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT'),
    r_description VARCHAR(255)
) ENGINE=InnoDB;

-- Insert default roles
INSERT INTO role (r_name, r_description) VALUES
('ORG_ADMIN', 'Full control over the organisation'),
('COURSE_EDITOR', 'Can manage courses and modules but not billing'),
('SUPPORT', 'Can view student progress but not edit content');


-- ==========================================
-- 2. TENANT CORE (Organisation & Profile)
-- ==========================================
CREATE TABLE organisation (
                              org_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              org_email VARCHAR(255) UNIQUE NOT NULL,
                              org_password VARCHAR(255) NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                              ended_at DATETIME NULL,
                              -- Index for soft-delete filtering & auth lookups
                              INDEX idx_org_status (ended_at, org_id),
                              INDEX idx_org_email (org_email)
) ENGINE=InnoDB;

CREATE TABLE organisation_subscription (
                                           os_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           os_org_id BIGINT NOT NULL,
                                           os_plan_id BIGINT NOT NULL,
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                                           ended_at DATETIME NULL,
                                           os_status TINYINT(1) DEFAULT 1,
                                           CONSTRAINT fk_os_org FOREIGN KEY (os_org_id) REFERENCES organisation(org_id),
                                           CONSTRAINT fk_os_plan FOREIGN KEY (os_plan_id) REFERENCES subscription_plan(sp_id),
                                           INDEX idx_os_org_active (os_org_id, os_status)
) ENGINE=InnoDB;

CREATE TABLE profile (
                         p_org_id BIGINT PRIMARY KEY,
                         p_org_name VARCHAR(255) NOT NULL,
                         p_org_reg_number VARCHAR(100),
                         p_org_vat_number VARCHAR(100),
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_profile_org FOREIGN KEY (p_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;


-- 2.1 Staff (The 'Proxy' Users)
CREATE TABLE staff (
    stf_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stf_org_id BIGINT NOT NULL,
    stf_role_id BIGINT NOT NULL,
    stf_email VARCHAR(255) NOT NULL,
    stf_password VARCHAR(255) NOT NULL,
    stf_is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    CONSTRAINT fk_staff_org FOREIGN KEY (stf_org_id) REFERENCES organisation(org_id),
    CONSTRAINT fk_staff_role FOREIGN KEY (stf_role_id) REFERENCES role(r_id),
    UNIQUE INDEX idx_staff_email_org (stf_email, stf_org_id)
) ENGINE=InnoDB;

-- ==========================================
-- 3. AUTHENTICATION (API Keys)
-- ==========================================
CREATE TABLE api_key (
                         ak_org_id BIGINT PRIMARY KEY,
                         ak_key_hash VARCHAR(255) UNIQUE NOT NULL, -- The hashed key for comparison
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_api_org FOREIGN KEY (ak_org_id) REFERENCES organisation(org_id),
                         -- Fast lookup for auth filter
                         INDEX idx_api_auth (ak_key_hash, ended_at, ak_org_id)
) ENGINE=InnoDB;

-- ==========================================
-- 4. COURSE HIERARCHY (Multi-Tenant)
-- ==========================================
CREATE TABLE course (
                        c_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        c_org_id BIGINT NOT NULL,
                        c_name VARCHAR(255) NOT NULL,
                        c_image_url VARCHAR(255), -- Optional: URL to course avatar image
                        c_description TEXT,
                        c_difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
                        c_tags VARCHAR(255),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        ended_at DATETIME NULL,
                        CONSTRAINT fk_course_org FOREIGN KEY (c_org_id) REFERENCES organisation(org_id),
                        -- Index for tenant-specific course management
                        INDEX idx_course_tenant (c_org_id, c_id),
                        INDEX idx_course_status (ended_at, c_id)
) ENGINE=InnoDB;

-- Enforcing 1 Module per Course via UNIQUE constraint
CREATE TABLE module (
                        m_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        m_course_id BIGINT
                            NOT NULL,
                        m_name VARCHAR(255) NOT NULL,
                        m_description TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        ended_at DATETIME NULL,
                        CONSTRAINT fk_module_course FOREIGN KEY (m_course_id) REFERENCES course(c_id),
                        -- Index for Course -> Module join
                        INDEX idx_module_course_fk (m_course_id),
                        INDEX idx_module_status (ended_at, m_id)
) ENGINE=InnoDB;

CREATE TABLE section (
                         s_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         s_module_id BIGINT NOT NULL,
                         s_title VARCHAR(255) NOT NULL,
                         s_content TEXT,
                         s_duration INT, -- Optional: Duration in seconds
                         s_resource_url VARCHAR(255), -- Optional: URL to external resource
                         s_resource_media_type VARCHAR(100), -- Optional: MIME type of resource (e.g., 'video/mp4')
                         s_order_index INT NOT NULL, -- For sorted lesson delivery
                         s_tags VARCHAR(255), -- Optional: Tags for filtering
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_section_module FOREIGN KEY (s_module_id) REFERENCES module(m_id),
                         -- Index for fetching sections in order
                         INDEX idx_section_order (s_module_id, s_order_index),
                         INDEX idx_section_status (ended_at, s_id)
) ENGINE=InnoDB;

-- ==========================================
-- 5. STUDENTS (Tenant Bound)
-- ==========================================
CREATE TABLE student (
                         st_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         st_org_id BIGINT NOT NULL,
                         st_student_number VARCHAR(100) NOT NULL,
                         st_first_name VARCHAR(100),
                         st_last_name VARCHAR(100),
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         ended_at DATETIME NULL,
                         CONSTRAINT fk_student_org FOREIGN KEY (st_org_id) REFERENCES organisation(org_id),
                         -- Most common student lookup: "Does this student exist in THIS org?"
                         UNIQUE INDEX idx_student_tenant_auth (st_org_id, st_student_number)
) ENGINE=InnoDB;

CREATE TABLE student_enrollment (
                                    se_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    se_student_id BIGINT NOT NULL,
                                    se_course_id BIGINT NOT NULL,
                                    se_enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    se_completed_at DATETIME NULL,
                                    CONSTRAINT fk_se_student FOREIGN KEY (se_student_id) REFERENCES student(st_id),
                                    CONSTRAINT fk_se_course FOREIGN KEY (se_course_id) REFERENCES course(c_id)
) ENGINE=InnoDB;

CREATE TABLE student_progress (
                                  sp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  sp_student_enrollment_id BIGINT NULL,
                                  sp_section_id BIGINT NULL,
                                  sp_percentage DECIMAL(5,2),
                                  sp_updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_sp_enrollment FOREIGN KEY (sp_student_enrollment_id) REFERENCES student_enrollment(se_id),
                                  CONSTRAINT fk_sp_section FOREIGN KEY (sp_section_id) REFERENCES section(s_id)
) ENGINE=InnoDB;