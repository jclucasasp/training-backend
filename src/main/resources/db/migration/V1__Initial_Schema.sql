-- 1. Base Tables (No Dependencies)
CREATE TABLE Organisation (
                              org_id INT AUTO_INCREMENT PRIMARY KEY,
                              org_email VARCHAR(255) UNIQUE NOT NULL,
                              org_password VARCHAR(255) NOT NULL,
                              org_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              org_ended_at DATETIME NULL,
                              org_password_reset_date DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE Role (
                      r_id INT AUTO_INCREMENT PRIMARY KEY,
                      r_admin TINYINT(1) DEFAULT 0,
                      r_editor TINYINT(1) DEFAULT 0,
                      r_user TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE Course (
                        c_id INT AUTO_INCREMENT PRIMARY KEY,
                        c_name VARCHAR(255) NOT NULL,
                        c_description TEXT,
                        c_difficulty VARCHAR(50),
                        c_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        c_ended_at DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE Module (
                        m_id INT AUTO_INCREMENT PRIMARY KEY,
                        m_name VARCHAR(255) NOT NULL,
                        m_description TEXT,
                        m_duration INT,
                        m_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        m_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                        m_ended_at DATETIME NULL,
                        m_tags TEXT
) ENGINE=InnoDB;

CREATE TABLE Section (
                         s_id INT AUTO_INCREMENT PRIMARY KEY,
                         s_description TEXT,
                         s_duration INT,
                         s_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         s_updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                         s_ended_at DATETIME NULL,
                         s_tags TEXT
) ENGINE=InnoDB;

CREATE TABLE Api_Key (
                         ak_id INT AUTO_INCREMENT PRIMARY KEY,
                         ak_value VARCHAR(255) UNIQUE NOT NULL,
                         ak_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         ak_ended_at DATETIME NULL
) ENGINE=InnoDB;

-- 2. First Level Relationship Tables
CREATE TABLE Role_Rel (
                          rr_id INT AUTO_INCREMENT PRIMARY KEY,
                          rr_org_id INT NULL,
                          rr_role_id INT NULL,
                          CONSTRAINT fk_rr_org FOREIGN KEY (rr_org_id) REFERENCES Organisation(org_id) ON DELETE SET NULL,
                          CONSTRAINT fk_rr_role FOREIGN KEY (rr_role_id) REFERENCES Role(r_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Org_Api_Rel (
                             oar_id INT AUTO_INCREMENT PRIMARY KEY,
                             oar_org_id INT NULL,
                             oar_key_id INT NULL,
                             CONSTRAINT fk_oar_org FOREIGN KEY (oar_org_id) REFERENCES Organisation(org_id) ON DELETE SET NULL,
                             CONSTRAINT fk_oar_key FOREIGN KEY (oar_key_id) REFERENCES Api_Key(ak_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Org_Course_Rel (
                                ocr_id INT AUTO_INCREMENT PRIMARY KEY,
                                ocr_org_id INT NULL,
                                ocr_course_id INT NULL,
                                CONSTRAINT fk_ocr_org FOREIGN KEY (ocr_org_id) REFERENCES Organisation(org_id) ON DELETE SET NULL,
                                CONSTRAINT fk_ocr_course FOREIGN KEY (ocr_course_id) REFERENCES Course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Course_Module_Rel (
                                   cmr_id INT AUTO_INCREMENT PRIMARY KEY,
                                   cmr_course_id INT NULL,
                                   cmr_module_id INT NULL,
                                   CONSTRAINT fk_cmr_course FOREIGN KEY (cmr_course_id) REFERENCES Course(c_id) ON DELETE SET NULL,
                                   CONSTRAINT fk_cmr_module FOREIGN KEY (cmr_module_id) REFERENCES Module(m_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Module_Section_Rel (
                                    msr_id INT AUTO_INCREMENT PRIMARY KEY,
                                    msr_module_id INT NULL,
                                    msr_section_id INT NULL,
                                    CONSTRAINT fk_msr_module FOREIGN KEY (msr_module_id) REFERENCES Module(m_id) ON DELETE SET NULL,
                                    CONSTRAINT fk_msr_section FOREIGN KEY (msr_section_id) REFERENCES Section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Asset (
                       a_id INT AUTO_INCREMENT PRIMARY KEY,
                       a_course_id INT NULL,
                       a_file_url TEXT NOT NULL,
                       a_file_type VARCHAR(50),
                       CONSTRAINT fk_a_course FOREIGN KEY (a_course_id) REFERENCES Course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3. Second Level Relationship Tables
CREATE TABLE Profile (
                         p_org_id INT NULL,
                         p_org_name VARCHAR(255),
                         p_org_role_id INT NULL,
                         p_org_reg_number VARCHAR(100),
                         p_org_vat_number VARCHAR(100),
                         p_org_updated_at DATETIME NULL,
                         CONSTRAINT fk_p_org FOREIGN KEY (p_org_id) REFERENCES Organisation(org_id) ON DELETE SET NULL ,
                         CONSTRAINT fk_p_role_rel FOREIGN KEY (p_org_role_id) REFERENCES Role_Rel(rr_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Section_Asset_Rel (
                                   sar_id INT AUTO_INCREMENT PRIMARY KEY,
                                   sar_asset_id INT NULL,
                                   sar_section_id INT NULL,
                                   CONSTRAINT fk_sar_asset FOREIGN KEY (sar_asset_id) REFERENCES Asset(a_id) ON DELETE SET NULL,
                                   CONSTRAINT fk_sar_section FOREIGN KEY (sar_section_id) REFERENCES Section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Student (
                         st_id INT AUTO_INCREMENT PRIMARY KEY,
                         st_org_id INT NULL,
                         st_student_number VARCHAR(100) UNIQUE NOT NULL,
                         st_api_key_id INT NULL,
                         st_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         st_ended_at DATETIME NULL,
                         CONSTRAINT fk_st_org FOREIGN KEY (st_org_id) REFERENCES Organisation(org_id) ON DELETE SET NULL,
                         CONSTRAINT fk_st_api_key FOREIGN KEY (st_api_key_id) REFERENCES Api_Key(ak_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Third Level Relationship Tables
CREATE TABLE Student_Enrollment (
                                    se_id INT AUTO_INCREMENT PRIMARY KEY,
                                    se_student_number VARCHAR(100) NULL,
                                    se_course_id INT NULL,
                                    se_enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    se_completed_at DATETIME NULL,
                                    CONSTRAINT fk_se_student FOREIGN KEY (se_student_number) REFERENCES Student(st_student_number) ON DELETE SET NULL,
                                    CONSTRAINT fk_se_course FOREIGN KEY (se_course_id) REFERENCES Course(c_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. Final Level Relationship Tables
CREATE TABLE Student_Progress (
                                  sp_id INT AUTO_INCREMENT PRIMARY KEY,
                                  sp_student_enrollment_id INT NULL,
                                  sp_module_id INT NULL,
                                  sp_section_id INT NULL,
                                  sp_percentage DECIMAL(5,2),
                                  sp_updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_sp_enrollment FOREIGN KEY (sp_student_enrollment_id) REFERENCES Student_Enrollment(se_id) ON DELETE SET NULL,
                                  CONSTRAINT fk_sp_module FOREIGN KEY (sp_module_id) REFERENCES Module(m_id) ON DELETE SET NULL,
                                  CONSTRAINT fk_sp_section FOREIGN KEY (sp_section_id) REFERENCES Section(s_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Speed up lookups for student numbers and emails
CREATE INDEX idx_student_number ON Student(st_student_number);
CREATE INDEX idx_org_email ON Organisation(org_email);

-- Index Foreign Keys that will be used in frequent JOINs
CREATE INDEX idx_enrollment_course ON Student_Enrollment(se_course_id);
CREATE INDEX idx_enrollment_student ON Student_Enrollment(se_student_number);
CREATE INDEX idx_progress_enrollment ON Student_Progress(sp_student_enrollment_id);

-- Speed up search by Course and Module names
CREATE INDEX idx_course_name ON Course(c_name);
CREATE INDEX idx_module_name ON Module(m_name);