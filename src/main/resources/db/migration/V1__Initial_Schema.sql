-- 1. Subscription Plans (The "Catalog")
CREATE TABLE subscription_plan (
                                   sp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   sp_name VARCHAR(50) NOT NULL,
                                   sp_price DECIMAL(10, 2) NOT NULL,
                                   sp_course_limit INT DEFAULT 5,
                                   sp_is_active TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

-- 2. Base Organisation Table (Multi-Tenant Root)
CREATE TABLE organisation (
                              org_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              org_email VARCHAR(255) UNIQUE NOT NULL,
                              org_password VARCHAR(255) NOT NULL,
                              org_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              org_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    -- If ended_at is NOT NULL, the organisation and all its child data are "disabled"
                              org_ended_at DATETIME NULL
) ENGINE=InnoDB;

-- 3. Active Subscriptions
CREATE TABLE organisation_subscription (
                                           os_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           os_org_id BIGINT NOT NULL,
                                           os_plan_id BIGINT NOT NULL,
                                           os_start_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           os_end_date DATETIME NOT NULL,
                                           os_status VARCHAR(20) DEFAULT 'ACTIVE',
                                           CONSTRAINT fk_os_org FOREIGN KEY (os_org_id) REFERENCES organisation(org_id),
                                           CONSTRAINT fk_os_plan FOREIGN KEY (os_plan_id) REFERENCES subscription_plan(sp_id)
) ENGINE=InnoDB;

-- 4. API Keys
CREATE TABLE api_key (
                         ak_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ak_org_id BIGINT NOT NULL,
                         ak_key_hash VARCHAR(255) UNIQUE NOT NULL,
                         ak_name VARCHAR(100),
                         ak_is_active TINYINT(1) DEFAULT 1,
                         ak_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_api_org FOREIGN KEY (ak_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

-- 5. Profile (Shared PK with Org)
CREATE TABLE profile (
                         p_org_id BIGINT PRIMARY KEY,
                         p_org_name VARCHAR(255) NOT NULL,
                         p_org_reg_number VARCHAR(100),
                         p_org_vat_number VARCHAR(100),
                         p_org_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_profile_org FOREIGN KEY (p_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

-- 6. Course Hierarchy
CREATE TABLE course (
                        c_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        c_org_id BIGINT NOT NULL,
                        c_name VARCHAR(255) NOT NULL,
                        c_description TEXT,
                        c_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_course_org FOREIGN KEY (c_org_id) REFERENCES organisation(org_id)
) ENGINE=InnoDB;

CREATE TABLE module (
                        m_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        m_course_id BIGINT UNIQUE NOT NULL,
                        m_name VARCHAR(255) NOT NULL,
                        m_description TEXT,
                        CONSTRAINT fk_module_course FOREIGN KEY (m_course_id) REFERENCES course(c_id)
) ENGINE=InnoDB;

CREATE TABLE section (
                         s_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         s_module_id BIGINT NOT NULL,
                         s_title VARCHAR(255) NOT NULL,
                         s_content TEXT,
                         s_order_index INT NOT NULL,
                         CONSTRAINT fk_section_module FOREIGN KEY (s_module_id) REFERENCES module(m_id)
) ENGINE=InnoDB;

-- 7. Students
CREATE TABLE student (
                         st_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         st_org_id BIGINT NOT NULL,
                         st_student_number VARCHAR(100) NOT NULL,
                         st_first_name VARCHAR(100),
                         st_last_name VARCHAR(100),
                         CONSTRAINT fk_student_org FOREIGN KEY (st_org_id) REFERENCES organisation(org_id),
                         UNIQUE (st_org_id, st_student_number)
) ENGINE=InnoDB;