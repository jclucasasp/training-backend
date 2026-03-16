# 🛠 AR Backend Database Technical Documentation

This document provides a comprehensive breakdown of the database schema. It is designed to ensure engineers understand the data constraints, the multi-tenant architecture, and the lifecycle of learning content.

---

## 1. Global Architectural Standards

### 🛡 Multi-Tenancy & Isolation
The system uses a **Shared Schema** model. Every organization (tenant) has its own data, separated by an `org_id`.
* **The Rule:** Any table with an `_org_id` or similar column (e.g., `cou_org_id`, `stu_org_id`) must be queried using that ID.
* **Implementation:** Cross-tenant data access is a critical security failure. All Repository methods must filter by the Organization ID.

### ♻️ Lifecycle & Auditing
* **Soft Deletes:** We use the `ended_at` timestamp. A record is only "active" if `ended_at` is `NULL`.
* **Timestamps:** `created_at` and `updated_at` are automatically managed to provide a full audit trail of record modifications.

---

## 2. Entity Modules & Relationships

### Module A: Global & Administrative (3 Tables)
1.  **`subscription_plan`**: Stores global tiers (Monthly/Yearly) and their associated course limits.
2.  **`role`**: Defines system-wide permissions (ORG_ADMIN, COURSE_EDITOR, etc.).
3.  **`api_key`**: Stores hashed keys for external integrations, linked to an organization.

### Module B: Tenant Core (4 Tables)
4.  **`organisation`**: The root entity for a tenant/business.
5.  **`organisation_subscription`**: Junction table linking an organisation to its active `subscription_plan`.
6.  **`profile`**: Extension of organisation for business metadata (VAT, Registration numbers).
7.  **`address`**: Physical location details linked to an organisation.

### Module C: Human Resources (2 Tables)
8.  **`staff`**: Accounts for employees of an organisation (Administrators and Editors).
9.  **`staff_role`**: Junction table linking staff members to their specific security `role`.

### Module D: Curriculum & Content (4 Tables)
10. **`course`**: The top-level learning container (requires a unique `cou_slug`).
11. **`chapter`**: Logical modules within a course.
12. **`chapter_section`**: The individual lessons containing content and media URLs.
13. **`attachment`**: Downloadable resources or 3D assets linked to a `chapter_section`.

### Module E: Assessment Engine (6 Tables)
14. **`quiz`**: Stores passing scores and attempt limits.
15. **`quiz_question`**: Individual questions linked to a quiz.
16. **`quiz_question_option`**: The multiple-choice options for a question.
17. **`chapter_quizzes`**: **Junction table** linking a quiz to one or more chapters.
18. **`student_quizzes`**: **Registration table**; authorizes a specific student to take a specific quiz.
19. **`student_quiz_attempt`**: The history of a quiz execution, including the serialized JSON answers.

### Module F: Student Tracking (3 Tables)
20. **`student`**: The learner profile within an organisation.
21. **`student_enrollment`**: Links a student to a `course` and tracks total percentage progress.
22. **`student_progress`**: Tracks the completion status of individual `chapter_section` records.

### Module G: Communication (1 Table)
23** `course_question`**: Handles Q&A within a course. Uses a polymorphic design to allow either `staff` or `student` to be the author.
24**`course_question_reply`**: Handles Q&A within a course. Uses a polymorphic design to allow either `staff` or `student` to be the author.
---

## 3. Operational Data Flow (Logic Sequence)

1.  **Onboarding:** An `organisation` is created + `profile` and `subscription_plan` are linked.
2.  **Authoring:** `staff` create a `course`. They then create `chapter`s and `chapter_section`s.
3.  **Quiz Setup:** A `quiz` is created. It is then mapped to a specific chapter via the `chapter_quizzes` table.
4.  **Student Enrollment:** A `student` is registered to the organization and then enrolled in a `course` via `student_enrollment`.
5.  **Quiz Registration:** Before taking a test, a student is registered via `student_quizzes`.
6.  **Learning:** As the student views sections, `student_progress` records are created.
7.  **Assessment:** The student submits a quiz. The `QuizService` validates the registration in `student_quizzes`, grades the attempt, and saves the final result in `student_quiz_attempt`.
8.  **Q&A:** Students can ask questions via `course_question` and the tutors can reply via `course_question_reply`.

---

## 4. Constraint Mapping Reference

| Source Column     | Target Column           | Constraint Type         |
|:------------------|:------------------------|:------------------------|
| `stf_org_id`      | `organisation.org_id`   | Foreign Key (Isolation) |
| `cou_slug`        | N/A                     | Unique (within Org)     |
| `sqa_quiz_id`     | `quiz.qui_id`           | Foreign Key (Audit)     |
| `cqr_question_id` | `course_question.cq_id` | Cascade Delete          |

---
*Generated for the AR Backend Engineering Team - March 2026*