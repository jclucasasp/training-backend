-- ==========================================================
-- 1. SEED DATA (Initial Setup)
-- ==========================================================

-- Basic Roles
INSERT INTO Role (r_id, r_admin, r_editor, r_user) VALUES
                                                       (1, 1, 0, 0), -- Super Admin
                                                       (2, 0, 1, 0), -- Content Editor
                                                       (3, 0, 0, 1); -- Standard User

-- Initial Organisation
INSERT INTO Organisation (org_id, org_email, org_password) VALUES
    (1, 'contact@acme-corp.com', 'hashed_password_here');

-- Link Org to Roles
INSERT INTO Role_Rel (rr_id, rr_org_id, rr_role_id) VALUES (1, 1, 1);

-- Profile Setup
INSERT INTO Profile (p_org_id, p_org_name, p_org_role_id, p_org_reg_number) VALUES
    (1, 'Acme Learning Academy', 1, 'REG-12345');

-- Sample Course & Content
INSERT INTO Course (c_id, c_name, c_description, c_difficulty) VALUES
    (101, 'Introduction to SQL', 'Learn the basics of relational databases.', 'Beginner');

INSERT INTO Module (m_id, m_name, m_description, m_duration) VALUES
    (501, 'Select Statements', 'How to query data.', 45);

INSERT INTO Section (s_id, s_description, s_duration) VALUES
    (901, 'The WHERE Clause', 15);

-- Link Course -> Module -> Section
INSERT INTO Course_Module_Rel (cmr_id, cmr_course_id, cmr_module_id) VALUES (1, 101, 501);
INSERT INTO Module_Section_Rel (msr_id, msr_module_id, msr_section_id) VALUES (1, 501, 901);
