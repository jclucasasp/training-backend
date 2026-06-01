# AR Backend: Front-End Integration Guide

## 1. Architecture & Stack
* **UI Framework:** React + shadcn/ui (Tailwind CSS).
* **3D Rendering:** React-Three-Fiber (R3F) + `@react-three/xr`.
* **State Management:** Redux Toolkit (for Auth & Course List) + R3F local state (for 3D assets).

## 2. Authentication & Multi-Tenancy
The backend uses a **Session-Based** auth strategy backed by Redis, not stateless JWTs.
1. **Login:** `POST /login` returns a `SESSION` cookie.
2. **Tenant Context:** The backend `TenantFilter` automatically extracts the `org_id` from the session. **The front-end does NOT need to manually append `org_id` to API requests.**
3. **API Keys:** For the public R3F viewer (unauthenticated users), pass the organization's API key in the header: `X-API-KEY`.

## 3. Data Hierarchy for R3F Scene Graph
Map the backend DTO hierarchy directly to your R3F `<group>` nesting:

| Backend DTO (`CourseResponse`) | R3F Component Mapping |
| :--- | :--- |
| `CourseResponse` | `<Canvas>` or Root `<group>` |
| `CourseChapterResponse` | `<group name="chapter">` (Logical folder) |
| `ChapterSectionResponse` | `<group name="section">` (Contains 3D assets) |
| `AttachmentResponse` | `<GLTFLoader>` or asset URL fetch |

**Fetch Strategy:** Use `GET /api/v1/courses/{slug}`. The backend uses `@NamedEntityGraph` to return the entire nested tree in one request. Do not fetch chapters/sections individually.

## 4. Core User Flows

### A. Staff Back-Office (`/admin`)
* **UI:** Use shadcn/ui `DataGrid` for course lists, `Form` for CRUD.
* **APIs:**
    * Create: `POST /api/v1/courses` (Send nested JSON, backend handles cascade save).
    * Read: `GET /api/v1/courses`
    * Delete: `DELETE /api/v1/courses/{id}` (Backend handles soft-delete via `StatusTypes.ARCHIVED`).

### B. Public AR/VR Viewer
* **UI:** Full-screen R3F `<Canvas>`.
* **Interaction:** Use `@react-three/xr` for VR/AR camera controls.
* **Asset Loading:** Fetch `AttachmentResponse` URLs and load them dynamically using Three.js loaders inside R3F `useLoader` or `Suspense` boundaries.

### C. Student Assessment
* **UI:** shadcn/ui `Card` and `RadioGroup` for quiz UI overlaid on the R3F canvas.
* **Submission:** `POST /api/v1/quizzes/{id}/submit` with `QuizSubmissionRequest`.
* **Important:** Do not calculate scores on the front-end. The backend `QuizService` grades attempts securely and returns `QuizResultResponse`.

## 5. API Conventions
* **Dates:** Serialized as `yyyy-MM-dd'T'HH:mm:ss` (SAST timezone). Parse with `date-fns` or `dayjs`.
* **Validation:** Backend uses Jakarta Validation. If a request fails, `GlobalExceptionHandler` returns a standardized `ErrorDetailsResponse` object. Handle these with shadcn/ui `Toast` notifications.
