# AR Backend Technical Documentation

This document outlines the system architecture, business logic, and user flows for the Multi-Tenant AR Backend.

---

## 1. Customer Journey
This journey tracks the lifecycle of the system from two primary perspectives: **Staff** (Content Creators) and **Students** (Consumers).

### Flow Overview
| Phase        | Actor   | Endpoint                           | Business Logic                                                                |
|:-------------|:--------|:-----------------------------------|:------------------------------------------------------------------------------|
| **Auth**     | All     | `POST /login`                      | Authenticates user; injects `org_id` into the security context.               |
| **Setup**    | Staff   | `POST /api/v1/courses`             | Creates the root entity. Slug is auto-generated from the name.                |
| **Build**    | Staff   | `POST /api/v1/quizzes`             | Defines assessment logic. Quizzes are linked to Chapters via junction tables. |
| **Enroll**   | Student | `POST /api/v1/student`             | Links a student to an organisation.                                           |
| **Study**    | Student | `GET /api/v1/courses/{slug}`       | Fetches curriculum. Filtered strictly by `org_id`.                            |
| **Evaluate** | Student | `POST /api/v1/quizzes/{id}/submit` | Grades answers server-side; persists attempt as JSON.                         |

---

## 2. Business System Roadmap (BSR)

### A. Multi-Tenant Architecture
The system utilizes a **shared-schema** multi-tenancy model. Every data table includes an `org_id` column to ensure data isolation.

* **Security Principle:** No entity is ever fetched by `id` alone. All Repository calls must use `findByIdAndOrganisationId`.
* **Tenant Propagation:** The `TenantProvider` utility extracts the `org_id` from the authenticated principal and propagates it through the Service layer.



### B. Content Hierarchy & Persistence
Curriculum data is structured as a tree. To prevent data fragmentation, the system uses the following hierarchy:
**Course** > **Chapter** > **Chapter Section** (+ **Quiz** via junction).

* **Cascading:** Deleting a Course invokes a `CASCADE` delete on all child Chapters and Sections.
* **Atomic Updates:** Course updates use `@Transactional` to ensure that if a nested Chapter update fails, the entire course state is rolled back.

### C. The Grading Engine
Assessment grading is handled exclusively on the backend to prevent tampering.
1.  **Lookups:** Questions are loaded into a `Map` for $O(1)$ access during grading.
2.  **Scoring:** Scores are calculated as a `BigDecimal` and compared against the `passing_score` threshold.
3.  **Audit Trail:** Student answers are serialized into a `JSON` string and stored in a `TEXT` column (`sqa_answers`).

---

## 3. System Sequence Diagrams

### Quiz Submission & Grading
This diagram illustrates the secure grading process and how state is persisted in the database.



### Nested Course Persistence
This diagram shows how MapStruct mappers and JPA repositories handle the complex creation of a Course with nested Chapters.



---

## 4. Engineer’s Definition of Done (DoD)

To maintain the integrity of the SQL schema and Swagger definitions, all new features must adhere to these standards:

| Requirement       | Implementation                                                                              |
|:------------------|:--------------------------------------------------------------------------------------------|
| **Tenant Safety** | Every SQL query MUST filter by `org_id`.                                                    |
| **Soft Deletes**  | Use the `ended_at` timestamp rather than `DELETE` for major entities.                       |
| **Validation**    | DTOs must use Jakarta Validation (e.g., `@NotBlank`, `@Size`).                              |
| **Audit Ready**   | Raw JSON of student interactions must be stored for disputed grades.                        |
| **API Sync**      | Swagger docs must be updated if DTO field names change (e.g., `displayName` vs `fileName`). |

---
*Generated for the AR Backend Engineering Team - March 2026*