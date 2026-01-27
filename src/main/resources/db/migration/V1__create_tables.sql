-- Database Schema for Course Management (MariaDB)

-- Create the Course table
CREATE TABLE Course (
                        c_id INT PRIMARY KEY,
                        c_name VARCHAR(255) NOT NULL,
                        c_description TEXT,
                        c_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        c_ended_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Module table
CREATE TABLE Module (
                        m_id INT PRIMARY KEY,
                        m_name VARCHAR(255) NOT NULL,
                        m_description TEXT,
                        m_duration INT,
                        m_tags VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Student_Progress table
CREATE TABLE Student_Progress (
                                  sp_id INT PRIMARY KEY,
                                  sp_module_id INT NOT NULL,
                                  sp_student_enrollment_id INT NOT NULL,
                                  sp_percentage DECIMAL(5,2),
                                  sp_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (sp_module_id) REFERENCES Module(m_id) ON DELETE CASCADE,
                                  FOREIGN KEY (sp_student_enrollment_id) REFERENCES Student_Enrollment(s_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Student_Enrollment table
CREATE TABLE Student_Enrollment (
                                    s_id INT PRIMARY KEY,
                                    s_course_id INT NOT NULL,
                                    s_student_number INT,
                                    s_enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    s_completed_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Section table
CREATE TABLE Section (
                         s_id INT PRIMARY KEY,
                         s_description TEXT,
                         s_duration INT,
                         s_tags VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Role table
CREATE TABLE Role (
                      r_id INT PRIMARY KEY,
                      r_name VARCHAR(255) NOT NULL,
                      r_description TEXT,
                      r_admin BOOLEAN,
                      r_editor BOOLEAN
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Organization table
CREATE TABLE Organization (
                              o_id INT PRIMARY KEY,
                              o_org_id VARCHAR(255) NOT NULL,
                              o_email VARCHAR(255),
                              o_password VARCHAR(255),
                              o_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              o_ended_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Asset table
CREATE TABLE Asset (
                       a_id INT PRIMARY KEY,
                       a_course_id INT NOT NULL,
                       a_file_url VARCHAR(255),
                       a_type VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create the Api_Rel table
CREATE TABLE Api_Rel (
                         ar_id INT PRIMARY KEY,
                         ar_org_id VARCHAR(255) NOT NULL,
                         ar_key_id INT NOT NULL,
                         ar_value INT,
                         ar_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         ar_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Recommended Indexes:

-- Index for frequently queried foreign keys
CREATE INDEX idx_organisation_id ON Organization (o_org_id);
CREATE INDEX idx_student_progress_module ON Student_Progress (sp_module_id);
CREATE INDEX idx_student_enrollment_course ON Student_Enrollment (s_course_id);

-- Additional Index (if needed based on queries)
-- CREATE INDEX idx_course_name ON Course (c_name);