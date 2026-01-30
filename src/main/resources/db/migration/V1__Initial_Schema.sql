-- 1. Base Tables (No Dependencies)
CREATE TABLE organisation (
                              org_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              org_email VARCHAR(255) UNIQUE NOT NULL,
                              org_password VARCHAR(255) NOT NULL,
                              org_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              org_ended_at DATETIME NULL,
                              org_password_reset_date DATETIME NULL,
                              org_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE role (
                      r_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      r_admin TINYINT(1) DEFAULT 0,
                      r_editor TINYINT(1) DEFAULT 0,
                      r_user TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE course (
                        c_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        c_name VARCHAR(255) NOT NULL,
                        c_description TEXT,
                        c_difficulty VARCHAR(50),
                        c_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        c_ended_at DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE module (
                        m_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        m_name VARCHAR(255) NOT NULL,
                        m_description TEXT,
                        m_duration INT,
                        m_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        m_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        m_ended_at DATETIME NULL,
                        m_tags TEXT
) ENGINE=InnoDB;

CREATE TABLE section (
                         s_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         s_description TEXT,
                         s_duration INT,
                         s_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         s_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         s_ended_at DATETIME NULL,
                         s_tags TEXT
) ENGINE=InnoDB;

CREATE TABLE api_key (
                         ak_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ak_value VARCHAR(255) UNIQUE NOT NULL,
                         ak_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         ak_ended_at DATETIME NULL
) ENGINE=InnoDB;

-- 2. First Level Relationship Tables
CREATE TABLE role_rel (
                          rr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          rr_org_id BIGINT NULL,
                          rr_role_id BIGINT NULL,
                          CONSTRAINT fk_rr_org FOREIGN KEY (rr_org_id) REFERENCES organisation(org_id) ON DELETE SET NULL,
                          CONSTRAINT fk_rr_role FOREIGN KEY (rr_role_id) REFERENCES role(r_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE org_api_rel (
                             oar_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             oar_org_id BIGINT NULL,
                             oar_key_id BIGINT NULL,
                             CONSTRAINT fk_oar_org FOREIGN KEY (oar_org_id) REFERENCES organisation(org_id) ON DELETE SET NULL,
                             CONSTRAINT fk_oar_key FOREIGN KEY (oar_key_id) REFERENCES api_key(ak_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE org_course_rel (
                                ocr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                ocr_org_id BIGINT NULL,
                                ocr_course_id BIGINT NULL,
                                CONSTRAINT fk_ocr_org FOREIGN KEY (ocr_org_id) REFERENCES organisation(org_id) ON DELETE SET NULL,
                                CONSTRAINT fk_ocr_course FOREIGN KEY (ocr_course_id) REFERENCES course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE course_module_rel (
                                   cmr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   cmr_course_id BIGINT NULL,
                                   cmr_module_id BIGINT NULL,
                                   CONSTRAINT fk_cmr_course FOREIGN KEY (cmr_course_id) REFERENCES course(c_id) ON DELETE SET NULL,
                                   CONSTRAINT fk_cmr_module FOREIGN KEY (cmr_module_id) REFERENCES module(m_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE module_section_rel (
                                    msr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    msr_module_id BIGINT NULL,
                                    msr_section_id BIGINT NULL,
                                    CONSTRAINT fk_msr_module FOREIGN KEY (msr_module_id) REFERENCES module(m_id) ON DELETE SET NULL,
                                    CONSTRAINT fk_msr_section FOREIGN KEY (msr_section_id) REFERENCES section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE asset (
                       a_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       a_course_id BIGINT NULL,
                       a_file_url TEXT NOT NULL,
                       a_file_type VARCHAR(50),
                       CONSTRAINT fk_a_course FOREIGN KEY (a_course_id) REFERENCES course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3. Second Level Relationship Tables
CREATE TABLE profile (
                         p_org_id BIGINT NULL,
                         p_org_name VARCHAR(255),
                         p_org_role_id BIGINT NULL,
                         p_org_reg_number VARCHAR(100),
                         p_org_vat_number VARCHAR(100),
                         p_org_updated_at DATETIME NULL,
                         CONSTRAINT fk_p_org FOREIGN KEY (p_org_id) REFERENCES organisation(org_id) ON DELETE SET NULL ,
                         CONSTRAINT fk_p_role_rel FOREIGN KEY (p_org_role_id) REFERENCES role_rel(rr_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE section_asset_rel (
                                   sar_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   sar_asset_id BIGINT NULL,
                                   sar_section_id BIGINT NULL,
                                   CONSTRAINT fk_sar_asset FOREIGN KEY (sar_asset_id) REFERENCES asset(a_id) ON DELETE SET NULL,
                                   CONSTRAINT fk_sar_section FOREIGN KEY (sar_section_id) REFERENCES section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE student (
                         st_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         st_org_id BIGINT NULL,
                         st_student_number VARCHAR(100) UNIQUE NOT NULL,
                         st_api_key_id BIGINT NULL,
                         st_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         st_ended_at DATETIME NULL,
                         CONSTRAINT fk_st_org FOREIGN KEY (st_org_id) REFERENCES organisation(org_id) ON DELETE SET NULL,
                         CONSTRAINT fk_st_api_key FOREIGN KEY (st_api_key_id) REFERENCES api_key(ak_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Third Level Relationship Tables
CREATE TABLE student_enrollment (
                                    se_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    se_student_number VARCHAR(100) NULL,
                                    se_course_id BIGINT NULL,
                                    se_enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    se_completed_at DATETIME NULL,
                                    CONSTRAINT fk_se_student FOREIGN KEY (se_student_number) REFERENCES student(st_student_number) ON DELETE SET NULL,
                                    CONSTRAINT fk_se_course FOREIGN KEY (se_course_id) REFERENCES course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. Final Level Relationship Tables
CREATE TABLE student_progress (
                                  sp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  sp_student_enrollment_id BIGINT NULL,
                                  sp_module_id BIGINT NULL,
                                  sp_section_id BIGINT NULL,
                                  sp_percentage DECIMAL(5,2),
                                  sp_updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_sp_enrollment FOREIGN KEY (sp_student_enrollment_id) REFERENCES student_enrollment(se_id) ON DELETE SET NULL,
                                  CONSTRAINT fk_sp_module FOREIGN KEY (sp_module_id) REFERENCES module(m_id) ON DELETE SET NULL,
                                  CONSTRAINT fk_sp_section FOREIGN KEY (sp_section_id) REFERENCES section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Speed up lookups for student numbers and emails
CREATE INDEX idx_student_number ON student(st_student_number);
CREATE INDEX idx_org_email ON organisation(org_email);

-- Index Foreign Keys that will be used in frequent JOINs
CREATE INDEX idx_enrollment_course ON student_enrollment(se_course_id);
CREATE INDEX idx_enrollment_student ON student_enrollment(se_student_number);
CREATE INDEX idx_progress_enrollment ON student_progress(sp_student_enrollment_id);

-- Speed up search by Course and Module names
CREATE INDEX idx_course_name ON course(c_name);
CREATE INDEX idx_module_name ON module(m_name);