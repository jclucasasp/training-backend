# LuminoEd Platform Product & Architecture Specification

This document serves as the official, comprehensive technical specification for the LuminoEd platform architecture and user journey flows. It integrates the baseline customer journey specifications, backend security filter mechanics, and recent B2B infrastructure decisions.

---

### 1. Corporate Registration & Subscription Lifecycle

*
**Initial Ingestion:** A client business or learning institute registers on the platform by submitting official organizational data, including their official company registration number, VAT number, and associated corporate credentials. Upon successful submission, the system generates an initial administrative username and password.


*
**Subscription Billing:** To activate full platform capabilities, the organization must complete a subscription purchase utilizing the integrated PayFast payment gateway loop.


*
**Real-time Registration Failures:** If a payment transaction fails during the initial online registration phase, the platform captures the gateway exception and notifies the organization immediately while they are actively online.


*
**Automated Renewal Failures:** If a subscription payment fails during a scheduled recurring billing cycle, the system automatically revokes the organization's master API key, flags the underlying corporate account status as delinquent, and dispatches an automated notification email directly to the organization's registered point of contact.


*
**Master Authorization Key:** Upon successful validation of an active subscription state, the system issues a master, cryptographically secure organization API key to be utilized exclusively for server-to-server operations.



---

### 2. Role-Based Access Control (RBAC) & Tenant Isolation

Administrative access to the authoring dashboard is governed by an explicit multi-tenant architecture backed by centralized state verification filters:

*
**Tenant Scoping:** Every incoming request passes through an internal security filter (`TenantFilter`) that establishes data separation boundaries by assigning the active organization identity via `TenantContext.setCurrentTenant(orgId)`.


*
**Web Portal Sessions:** Standard administrative staff and organization owners log into the web management dashboard using web browser sessions, which are backed by pre-populated session objects tracked inside a shared Redis data layer.


*
**Staff Provisioning:** Once an organization's subscription is confirmed active, the primary Organization Admin can provision internal staff profiles to build and maintain training assets, evaluation parameters, and student records.


*
**Permission Management:** Staff profile access structures and authorization bounds are entirely assigned, updated, and managed by the designated Organization Admin.


*
**Platform Roles:** The system enforces two distinct operational staff roles, though the underlying codebase remains flexible to adapt to changing corporate requirements over time:


*
**Course Editor:** Granted full authorization permissions to create, read, update, and delete (CRUD) courses, lesson nodes, and interactive evaluation quizzes.


*
**Support:** Restricted strictly to read-only views across the tenant's educational configurations and asset registries, with no data modification privileges.




*
**Filter Bypasses:** The security filter explicitly permits unauthenticated pass-through for public infrastructure endpoints, including OpenAPI schemas (`/v3/api-docs`, `/swagger-ui`), registration processing (`/api/v1/organisations/signup`), and payment notifications (`/api/v1/payments/itn`).



---

### 3. Student Just-In-Time (JIT) Provisioning Flow

Student accounts bypass standard frontend credential generation completely, relying instead on a secure, delegated authentication pattern initiated from the client company's internal infrastructure:

```
[ Learner Portal ] ---> (Click Course) ---> [ Client Server ]
                                                  |
                                           (Server Handshake)
                                           (Passes Master Key)
                                                  v
[ LuminoEd Frontend ] <--- (Launch URL) <-- [ LuminoEd Backend ]

```

*
**The Launch Event:** A learner logs into their organization's internal portal and chooses to execute a specific course hosted by LuminoEd.


*
**Server-to-Server Gatekeeper Handshake:** The client organization's backend captures the request and executes an immediate server-to-server HTTP POST request directly to the LuminoEd API gateway. This handshake passes the organization's master API key alongside unique learner metadata, specifically the student's unique identification number (employee or student number), first name, and surname. This design guarantees that the master API key is never leaked to the client-side browser or application network tabs.


*
**Credential Verification:** The LuminoEd gateway filter intercepts the server handshake request via the `X-API-KEY` header. The filter requires a well-formed token, extracting the initial 12 characters to serve as a high-speed prefix lookup key via an authorization lookup service. The key is checked against recorded records using a secure password encoder, and the active subscription status is programmatically confirmed.


*
**Dynamic Record Generation:** If the validated incoming student number does not correspond to an existing record within that tenant's database partition, the system automatically provisions a lean student record on the fly.


*
**Token Issuance:** Following successful JIT provisioning, the backend generates a unique, temporary student access token (temporary API key) along with a customized application launch URL pointing directly to the LuminoEd frontend environment, returning this payload to the client organization's host server.


*
**Session Lifespan Restrictions:** Every temporary student session token is bound to a strict Time-To-Live (TTL) duration of exactly 8 hours inside the system caches.


*
**Multi-Device Session Policy:** A student session token is authorized for uniform use across all hardware formats and connected devices simultaneously. This addresses cross-platform deployment since learners focus entirely on absorbing active courseware and are not expected to maintain multiple conflicting concurrent sessions.



---

### 4. Asset Streaming & Edge Architecture

Once the student is securely redirected to the LuminoEd application canvas, the system switches to a highly optimized asset delivery pipeline to handle intensive 3D point-cloud and geometry models:

*
**Manifest Loading:** The frontend application sends the temporary student token along with the student identification number to fetch the core course configuration JSON layout and necessary resource URLs.


*
**Token Verification Cache:** The short-lived student token is continuously evaluated with every inbound request, undergoing high-speed matching against a centralized Redis backend state cache.


*
**Scrape-Resistant Media Links:** When the 3D application canvas attempts to download point-cloud datasets (`.ksplat`) or binary models (`.glb`) hosted on the Cloudflare R2 bucket layer, it appends the temporary student token as a query parameter directly onto the resource URL string to prevent unauthorized resource scraping.


*
**Edge Worker Authorization:** A custom Cloudflare Worker intercepts all incoming asset download traffic prior to bucket execution. The Worker parses the token parameter, matches it against local edge key-value storage, and allows the request to proceed only upon a successful validation match.


*
**Global Caching Layer:** To maximize performance, minimize network latency, and reduce data bandwidth consumption during heavy 3D rendering loops, Cloudflare is configured to cache these asset binaries globally across regional edge network nodes.



---

### 5. Progress Resumption & Evaluation Workflows

*
**State Synchronization:** Returning students can query their historical learning profile by hitting the `/api/v1/studnet/{studentNumber}/dashboard` endpoint, which returns a structured collection of all currently enrolled training modules.


*
**Progress Resumption:** To continue an active training module, the application targets the `/api/v1/student/{studentNumber}/resume/{courseSlug}` endpoint, pulling down saved positioning coordinates and historical progress data.


*
**Quiz Submission:** Upon completing the interactive training milestones within the 3D scene, the user completes an evaluation quiz, pushing the calculated response logs directly to the frontend.


*
**Administrative Metrics Reporting:** Once a quiz is submitted, the evaluation analytics are automatically formatted and emailed to the specific internal staff member who originally generated the curriculum path.


*
**Managerial Dashboard Oversight:** In addition to automated email notifications, comprehensive student progress metrics and completion tracking records are stored natively within the platform database. Organization Admins and authorized staff members can inspect this historical telemetry at any time by logging into the management portal and viewing the specific employee or student tracking dashboard interface.



---

### 6. Deferred Product Roadmap (On-Demand Features)

To maintain a streamlined initial implementation scope, the following enterprise-tier capabilities are explicitly deferred and will be implemented strictly upon customer request:

*
**B2B Pre-Registration Systems:** Automated bulk-import mechanisms allowing enterprise clients to pre-allocate license seats and register corporate employee rosters via CSV or batch JSON schemas prior to launching a course are deferred, operating exclusively via the standard JIT pass-through ingestion model unless requested.


*
**Enterprise LMS Webhooks:** Deep external integrationswill only be developed upon explicit client request.