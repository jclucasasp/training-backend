- ==========================================================
-- VIEW: vw_student_detailed_progress
-- Provides a flattened look at student activity across the hierarchy
-- ==========================================================

CREATE OR REPLACE VIEW vw_student_detailed_progress AS
SELECT
    s.st_student_number AS student_number,
    o.org_email AS organisation_email,
    c.c_name AS course_name,
    c.c_difficulty AS difficultyTypes,
    m.m_name AS module_name,
    sec.s_description AS section_title,
    sp.sp_percentage AS progress_percentage,
    sp.sp_updated_at AS last_activity,
    -- Simple logic to check if a chapterSection is "passed"
    CASE
        WHEN sp.sp_percentage >= 100 THEN 'COMPLETED'
        WHEN sp.sp_percentage > 0 THEN 'IN_PROGRESS'
        ELSE 'NOT_STARTED'
    END AS status
FROM Student_Progress sp
JOIN Student_Enrollment se ON sp.sp_student_enrollment_id = se.se_id
JOIN Student s            ON se.se_student_number = s.st_student_number
JOIN Organisation o       ON s.st_org_id = o.org_id
JOIN Course c             ON se.se_course_id = c.c_id
JOIN Module m             ON sp.sp_module_id = m.m_id
JOIN Section sec          ON sp.sp_section_id = sec.s_id;