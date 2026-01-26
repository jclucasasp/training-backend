## 1. Executive Summary

This document defines the scope, architecture, data model, and implementation blueprint for a cloud‑native enterprise application that exposes a secure RESTful API to e‑learning partners. The service will deliver interactive AR/VR learning experiences to end‑users through a WebGL front‑end built with React‑Three‑Fiber (R3F). Backend services are built with Spring Boot, persist data in MariaDB, and cache frequently accessed objects in Redis. API keys are issued to partners and gate all external traffic.

---

## 2. Scope & Deliverables

| Area | Deliverable | Acceptance Criteria |
|------|-------------|---------------------|
| **API** | OAuth‑2.0‑style key‑based authentication, CRUD for courses, modules, assets, and users | 100 % test coverage, Swagger/OpenAPI spec, 99.9 % uptime |
| **Front‑end** | R3F‑powered AR/VR viewer, responsive UI, secure token exchange | Seamless experience on Chrome/Edge/Firefox (desktop & mobile) |
| **Data** | MariaDB schema, Redis caching strategy, data migration scripts | Schema enforces referential integrity, migrations roll‑back |
| **Infrastructure** | Docker images, Helm charts, CI/CD pipelines | Zero‑downtime deploys, auto‑scaling |
| **Monitoring** | Prometheus, Grafana dashboards, alerts | 95 % metric coverage, SLA monitoring |
| **Documentation** | Technical spec, API reference, deployment guide | Updated for each major version |

---

## 3. High‑Level Architecture

```
+-------------------------------------------------------------+
|                    Load Balancer / API Gateway              |
|   (NGINX/Traefik + TLS Termination)                         |
+------------------------+------------------------------------+
                         |
             +-----------+-----------+           
             |   Spring Boot API    |   
             |  (Service & Auth)    |  
             +-----------+-----------+   
                         |   Cache
+------------------------+--------------------+
|                Redis (Read‑through Cache)  |
+------------------------+--------------------+
                 |  
+----------------+----------------+    
|      MariaDB    |   (Post‑greSQL compatible)   |
|  (Primary & Read) |   (Primary + 2 replicas)    |
+----------------+----------------+
                 |
   +-------------+-------------+
   |     R3F Front‑End (SPA)  |
   |  (React, Three.js, XR)  |
   +-------------------------+
```

### 3.1 Component Responsibilities

| Component | Responsibility | Key Libraries / Tech |
|-----------|----------------|----------------------|
| **API Gateway** | TLS termination, rate‑limiting, request routing | NGINX/Traefik, OpenResty |
| **Auth Service** | API key issuance, key rotation, JWT token generation | Spring Security, JJWT |
| **Course Service** | CRUD for courses, modules, assets | Spring MVC, JPA/Hibernate |
| **Asset Service** | Stores URLs/metadata for 3D models, textures, videos | Spring MVC, Amazon S3 (optional) |
| **User Service** | Partner & learner management | Spring MVC, JPA |
| **Redis Cache** | Read‑through caching of course metadata, asset lists | Jedis / Lettuce |
| **MariaDB** | Persistent storage, relational data | JPA/Hibernate, Flyway |
| **Front‑End SPA** | Rendering AR/VR scenes, authentication UI | React, React‑Three‑Fiber, @react-three/xr |

---

## 4. Data Model & Schema

### 4.1 Entity Overview

| Entity | Description | Key Fields |
|--------|-------------|------------|
| `api_key` | Stores partner credentials | `id`, `key`, `secret`, `partner_id`, `scopes`, `status`, `created_at`, `expires_at` |
| `partner` | Represents an e‑learning provider | `id`, `name`, `email`, `org`, `status` |
| `course` | High‑level learning path | `id`, `partner_id`, `title`, `description`, `status`, `created_at` |
| `module` | Sub‑division of a course | `id`, `course_id`, `title`, `order`, `description` |
| `asset` | 3D/VR/AR model, video, image | `id`, `module_id`, `type`, `url`, `metadata_json`, `created_at` |
| `user` | End‑user of the platform | `id`, `email`, `first_name`, `last_name`, `role` |
| `enrollment` | Links users to courses | `id`, `user_id`, `course_id`, `progress`, `completed_at` |

### 4.2 ER Diagram (textual)

```
partner 1 --- * api_key
partner 1 --- * course
course 1 --- * module
module 1 --- * asset
user 1 --- * enrollment
course 1 --- * enrollment
```

### 4.3 MariaDB DDL

```sql
-- partners
CREATE TABLE partner (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  org VARCHAR(255),
  status ENUM('ACTIVE','SUSPENDED','DELETED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- api keys
CREATE TABLE api_key (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  key CHAR(32) NOT NULL UNIQUE,
  secret CHAR(64) NOT NULL,
  partner_id BIGINT UNSIGNED NOT NULL,
  scopes VARCHAR(255) NOT NULL,
  status ENUM('ACTIVE','REVOKED','EXPIRED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NULL,
  FOREIGN KEY (partner_id) REFERENCES partner(id)
) ENGINE=InnoDB;

-- courses
CREATE TABLE course (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  partner_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (partner_id) REFERENCES partner(id)
) ENGINE=InnoDB;

-- modules
CREATE TABLE module (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(255) NOT NULL,
  `order` INT UNSIGNED NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB;

-- assets
CREATE TABLE asset (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  module_id BIGINT UNSIGNED NOT NULL,
  type ENUM('MODEL','IMAGE','VIDEO','AUDIO','DOC') NOT NULL,
  url VARCHAR(512) NOT NULL,
  metadata_json JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (module_id) REFERENCES module(id)
) ENGINE=InnoDB;

-- users
CREATE TABLE user (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  role ENUM('ADMIN','TEACHER','STUDENT') DEFAULT 'STUDENT',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- enrollments
CREATE TABLE enrollment (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  course_id BIGINT UNSIGNED NOT NULL,
  progress DECIMAL(5,2) DEFAULT 0.00,
  completed_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB;
```

### 4.4 Indexing & Optimisation

| Table | Indexes |
|-------|---------|
| `api_key` | `(partner_id)`, `(status)`, `(expires_at)` |
| `course` | `(partner_id)`, `(status)`, `(created_at)` |
| `module` | `(course_id)`, `(order)` |
| `asset` | `(module_id)`, `(type)` |
| `enrollment` | `(user_id)`, `(course_id)`, `(completed_at)` |

Use MariaDB InnoDB compression for `asset.metadata_json` if storage grows.

---

## 5. Redis Caching Strategy

| Data | Cache Key | TTL | Use‑Case |
|------|-----------|-----|----------|
| Course & Module lists per partner | `partner:{id}:courses` | 12 h | Quick discovery |
| Asset list for a module | `module:{id}:assets` | 6 h | AR scene build |
| JWT token revocation list | `jwt:{jti}` | token expiry | Prevent replay |
| Rate‑limit counters per API key | `rl:{key}` | 1 min | Throttle requests |

Cache‑through pattern: Service first checks Redis; on miss, fetches from DB, writes to Redis, then returns.

---

## 6. API Design

### 6.1 Authentication Flow

1. **Key Rotation** – Partners can rotate keys via `/api/keys/rotate`.
2. **Token Exchange** – Partner sends `POST /auth/token` with `client_id` & `client_secret`.  
   Response: JWT access token (short‑lived) + refresh token (long‑lived).
3. **Token Refresh** – `POST /auth/refresh`.
4. **Revocation** – `POST /auth/revoke`.

JWT claims:
- `sub`: partner_id
- `scope`: list of scopes
- `jti`: token id (stored in Redis for revocation)

### 6.2 Resource End‑Points (REST + Swagger)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/courses` | List courses (paginated) | Partner |
| POST | `/courses` | Create course | Partner |
| GET | `/courses/{id}` | Course details | Partner |
| PUT | `/courses/{id}` | Update course | Partner |
| DELETE | `/courses/{id}` | Delete course | Partner |
| GET | `/courses/{id}/modules` | List modules | Partner |
| POST | `/courses/{id}/modules` | Add module | Partner |
| ... | ... | ... | ... |

All responses use HAL+JSON (`Link` headers) for discoverability.

### 6.3 Error Handling

Consistent JSON error format:

```json
{
  "timestamp": "2026-01-20T10:15:30Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "API key expired",
  "path": "/auth/token"
}
```

HTTP status codes:
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 429 Too Many Requests (rate limit)
- 500 Internal Server Error

---

## 7. Security Considerations

| Risk | Mitigation |
|------|------------|
| **API key leakage** | Store secrets in JCEKS/HashiCorp Vault, rotate keys, never log secrets |
| **Token replay** | Short‑lived JWTs + JTI revocation in Redis |
| **SQL Injection** | Use JPA with parameter binding, never concatenate SQL |
| **CSRF** | Token‑based auth (no cookies), use `SameSite=Strict` |
| **Rate‑limiting abuse** | IP + API key throttling via Redis |
| **XSS** | All client data encoded in SPA; use CSP header |
| **Denial‑of‑Service** | Circuit breaker pattern, horizontal scaling, Kubernetes HPA |

---

## 8. Front‑End Architecture (R3F)

1. **React Router** – SPA navigation.
2. **Redux Toolkit** – Global state (auth, course list).
3. **React‑Three‑Fiber** – 3D rendering.
4. **@react-three/xr** – WebXR polyfill for AR/VR.
5. **Three.js Loader** – GLTF/GLB, FBX, OBJ.
6. **Auth Flow** – Store JWT in HTTP‑Only cookie + refresh token.
7. **Error Boundary** – Capture rendering errors.
8. **Accessibility** – ARIA roles, keyboard navigation.

---

## 9. Infrastructure & DevOps

| Layer | Tool | Purpose |
|-------|------|---------|
| **Containerisation** | Docker, BuildKit | Reproducible builds |
| **Orchestration** | Kubernetes (EKS/GKE/Azure AKS) | Auto‑scaling, rolling updates |
| **Helm Charts** | 3rd‑party charts (MariaDB, Redis) + custom | Declarative infra |
| **CI/CD** | GitHub Actions / GitLab CI | Automated tests, build, push, deploy |
| **Monitoring** | Prometheus, Grafana | Metrics, logs |
| **Tracing** | OpenTelemetry + Jaeger | Distributed tracing |
| **Secrets** | HashiCorp Vault / KMS | Secure key storage |
| **Load Balancer** | Ingress controller (NGINX/Traefik) | TLS termination, path routing |

### 9.1 Deployment Pipeline

1. Commit → Pull Request → Code review
2. Automated unit & integration tests
3. Docker image build → Push to registry
4. Helm chart deploy (k8s)
5. Smoke tests (API health check)
6. Promote to prod if all checks pass

---

## 10. Scalability & Performance

| Dimension | Strategy |
|-----------|----------|
| **Read scaling** | MariaDB read replicas, Redis read replicas |
| **Write scaling** | Single primary + write‑through caching |
| **API throughput** | Horizontal scaling, rate‑limit per partner |
| **Asset delivery** | CDN (e.g., CloudFront) for static 3D models |
| **AR/VR streaming** | WebGL progressive loading, LOD (Level‑of‑Detail) |
| **Event‑driven** | Use Kafka for analytics (enrollments, progress) |

---

## 11. Maintenance & Support

| Area | Plan |
|------|------|
| **Bug tracking** | JIRA / GitHub Issues |
| **Release notes** | Keep changelog in Git repo, automated release notes |
| **Backups** | Daily logical backups (mysqldump) + incremental snapshots |
| **Monitoring** | Alerting on CPU > 80 %, Memory > 90 %, DB connection leaks |
| **Documentation** | Swagger UI + Confluence wiki |

---

## 12. Future Enhancements (Road‑Map)

| Phase | Feature | Benefit |
|-------|---------|---------|
| **Q1 2026** | Multi‑tenant support (isolation per partner) | Enterprise growth |
| **Q3 2026** | AI‑guided lessons (OpenAI GPT embeddings) | Adaptive learning |
| **Q1 2027** | Native mobile AR (ARKit / ARCore) | Wider reach |
| **Q3 2027** | Analytics dashboard (Engagement, retention) | Business insight |

---

## 13. Summary

- **Backend**: Spring Boot + JPA/Hibernate, MariaDB, Redis caching, JWT + API‑key auth.
- **Frontend**: React + R3F, WebXR, secure token handling.
- **APIs**: RESTful, authenticated, documented via OpenAPI.
- **Storage**: Relational DB for core data, Redis for caching, CDN for static assets.
- **Deployment**: Docker + Kubernetes + Helm, CI/CD, observability stack.

This architecture delivers a secure, scalable, and maintainable foundation for an AR/VR learning platform that can grow with enterprise partners while keeping the user experience smooth and immersive.

## Project Dependancies:

* **Spring Boot 3.x** (JDK 17+)
* **MariaDB + Flyway** (schema migrations)
* **Redis** (read‑through cache)
* **RabbitMQ** (event bus)
* **JWT + API‑Key** auth
* **Spring Data JPA** + **Lombok**
* **Swagger/OpenAPI** (auto‑generated docs)
* **Docker & Docker‑Compose** – one‑click dev stack

> **TL;DR** – copy the files into a Maven project, run `docker‑compose up`, then hit `http://localhost:8080/swagger-ui.html`.

---

## 1️⃣  Project Skeleton

```
spring‑boot‑ar‑backend/
 ├─ src/
 │   ├─ main/
 │   │   ├─ java/com/example/arbackend/
 │   │   │   ├─ config/
 │   │   │   │   ├─ SecurityConfig.java
 │   │   │   │   ├─ RedisConfig.java
 │   │   │   │   ├─ RabbitConfig.java
 │   │   │   │   └─ SwaggerConfig.java
 │   │   │   ├─ controller/
 │   │   │   │   └─ CourseController.java
 │   │   │   ├─ dto/
 │   │   │   │   └─ CourseDTO.java
 │   │   │   ├─ entity/
 │   │   │   │   ├─ ApiKey.java
 │   │   │   │   ├─ Course.java
 │   │   │   │   ├─ Module.java
 │   │   │   │   └─ Asset.java
 │   │   │   ├─ repository/
 │   │   │   │   ├─ ApiKeyRepository.java
 │   │   │   │   ├─ CourseRepository.java
 │   │   │   │   ├─ ModuleRepository.java
 │   │   │   │   └─ AssetRepository.java
 │   │   │   ├─ service/
 │   │   │   │   ├─ ApiKeyService.java
 │   │   │   │   ├─ CourseService.java
 │   │   │   │   ├─ ModuleService.java
 │   │   │   │   └─ AssetService.java
 │   │   │   ├─ util/
 │   │   │   │   ├─ JwtUtil.java
 │   │   │   │   └─ ApiKeyAuthFilter.java
 │   │   │   └─ ArBackendApplication.java
 │   ├─ main/resources/
 │   │   ├─ application.yml
 │   │   └─ logback-spring.xml
 │   └─ test/java/com/example/arbackend/
 │       └─ (add your tests here)
 ├─ Dockerfile
 ├─ docker‑compose.yml
 └─ pom.xml
```

> ⚡️ **Tip** – The `docker‑compose.yml` boots a MariaDB, Redis & RabbitMQ instance automatically.  
> **Security** – do **not** commit secrets to VCS. Use environment variables or a Vault solution.

---

## 2️⃣  Maven `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
           https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>ar-backend</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>ar-backend</name>
  <description>Spring‑Boot boilerplate for AR/VR learning platform</description>
  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.1.3</spring-boot.version>
  </properties>

  <dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>

    <!-- MariaDB Driver -->
    <dependency>
      <groupId>org.mariadb.jdbc</groupId>
      <artifactId>mariadb-java-client</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Flyway for DB migrations -->
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <!-- JWT -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>

    <!-- Swagger/OpenAPI -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.1.0</version>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## 3️⃣  `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mariadb://mariadb:3306/ar_db?useSSL=false&serverTimezone=UTC
    username: ar_user
    password: ${DB_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none          # Flyway handles schema
    properties:
      hibernate:
        format_sql: true
  redis:
    host: redis
    port: 6379
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest
  flyway:
    url: jdbc:mariadb://mariadb:3306/ar_db
    user: ar_user
    password: ${DB_PASSWORD}
    locations: classpath:db/migration
logging:
  level:
    org.springframework.amqp: DEBUG
server:
  port: 8080
jwt:
  issuer: ar-backend
  secret: ${JWT_SECRET}          # base64‑encoded
  expiration-ms: 3600000         # 1 h
  refresh-expiration-ms: 604800000   # 7 d
api:
  key-expiration-hours: 720
```

> **⚠️** Replace `${DB_PASSWORD}` & `${JWT_SECRET}` with a secure secret manager or env‑var loader.  
> Docker‑compose will export those as env‑vars for the container.

---

## 4️⃣  Entities & Repositories

```java
// src/main/java/com/example/arbackend/entity/ApiKey.java
package com.example.arbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "api_keys")
public class ApiKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;      // client_id

    @Column(nullable = false)
    private String secret;   // client_secret (hashed if you like)

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
```

```java
// src/main/java/com/example/arbackend/entity/Course.java
package com.example.arbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long partnerId; // FK to the partner that owns the course

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

> *Similarly* create `Module` and `Asset` tables; for brevity only `Course` is shown.

```java
// Repositories – just Spring Data interfaces
package com.example.arbackend.repository;

import com.example.arbackend.entity.ApiKey;
import com.example.arbackend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKey(String key);
}
```

---

## 5️⃣  Service Layer

```java
// src/main/java/com/example/arbackend/service/ApiKeyService.java
package com.example.arbackend.service;

import com.example.arbackend.entity.ApiKey;
import com.example.arbackend.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApiKeyService {
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public Optional<ApiKey> validate(String key, String secret) {
        return apiKeyRepository.findByKey(key)
                .filter(k -> k.getSecret().equals(secret) && k.getActive())
                .filter(k -> k.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    public ApiKey rotate(String key) {
        var apiKey = apiKeyRepository.findByKey(key).orElseThrow();
        apiKey.setExpiresAt(LocalDateTime.now().plusHours(
                Long.parseLong(System.getenv("API_KEY_EXPIRATION_HOURS"))
        ));
        return apiKeyRepository.save(apiKey);
    }
}
```

```java
// src/main/java/com/example/arbackend/service/CourseService.java
package com.example.arbackend.service;

import com.example.arbackend.entity.Course;
import com.example.arbackend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public Course create(Course course) {
        return courseRepository.save(course);
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    // add update, delete, etc. – all delegating to repo
}
```

---

## 6️⃣  Security Configuration

```java
// src/main/java/com/example/arbackend/config/SecurityConfig.java
package com.example.arbackend.config;

import com.example.arbackend.util.ApiKeyAuthFilter;
import com.example.arbackend.util.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.http.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtUtil jwtUtil,
                                           ApiKeyAuthFilter apiKeyFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter(jwtUtil), ApiKeyAuthFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter(ApiKeyService apiKeyService) {
        return new ApiKeyAuthFilter(apiKeyService);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
```

```java
// src/main/java/com/example/arbackend/util/JwtUtil.java
package com.example.arbackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String base64Secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    public String generateToken(String subject, Long partnerId) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .claim("partnerId", partnerId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String subject, Long partnerId) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .claim("partnerId", partnerId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

```java
// src/main/java/com/example/arbackend/util/ApiKeyAuthFilter.java
package com.example.arbackend.util;

import com.example.arbackend.entity.ApiKey;
import com.example.arbackend.service.ApiKeyService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = request.getHeader("X-API-KEY");
        String secret = request.getHeader("X-API-SECRET");

        if (key != null && secret != null) {
            Optional<ApiKey> optKey = apiKeyService.validate(key, secret);
            if (optKey.isPresent()) {
                // Put partnerId into SecurityContext for downstream auth
                ApiKey apiKey = optKey.get();
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("PARTNER_"+apiKey.getPartnerId()));
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        apiKey.getPartnerId(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 5️⃣  Controllers (Sample)

```java
// src/main/java/com/example/arbackend/controller/CourseController.java
package com.example.arbackend.controller;

import com.example.arbackend.dto.CourseDTO;
import com.example.arbackend.entity.Course;
import com.example.arbackend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<CourseDTO> create(@RequestBody CourseDTO dto) {
        Course created = courseService.create(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseDTO.fromEntity(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<CourseDTO> get(@PathVariable Long id) {
        return courseService.findById(id)
                .map(c -> ResponseEntity.ok(CourseDTO.fromEntity(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all courses for partner")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<CourseDTO> list(@RequestHeader("X-API-KEY") String key) {
        Long partnerId = getPartnerIdFromKey(key);
        return courseService.listByPartner(partnerId)
                .stream()
                .map(CourseDTO::fromEntity)
                .toList();
    }

    // Helper: parse partnerId from key (in production, use a service)
    private Long getPartnerIdFromKey(String key) {
        // TODO: replace with lookup from ApiKeyService
        return 1L; // placeholder
    }
}
```

```java
// DTO – simple mapping
package com.example.arbackend.dto;

import com.example.arbackend.entity.Course;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Long partnerId;

    public Course toEntity() {
        return Course.builder()
                .id(id)
                .title(title)
                .description(description)
                .partnerId(partnerId)
                .build();
    }

    public static CourseDTO fromEntity(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPartnerId()
        );
    }
}
```

---

## 6️⃣  Swagger/OpenAPI

```java
// src/main/java/com/example/arbackend/config/SwaggerConfig.java
package com.example.arbackend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Partner API")
                        .version("1.0")
                        .description("RESTful API for partner apps")
                );
    }
}
```

---

## 7️⃣  Dockerfile & Docker Compose

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/partnerdb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: example
      JWT_SECRET: ${JWT_SECRET}
      API_KEY_EXPIRATION_HOURS: 24
    depends_on:
      - db
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: example
      MYSQL_DATABASE: partnerdb
    ports:
      - "3306:3306"
    volumes:
      - db-data:/var/lib/mysql
volumes:
  db-data:
```

---

## 8️⃣  Unit Test Skeleton

```java
// src/test/java/com/example/arbackend/service/CourseServiceTest.java
package com.example.arbackend.service;

import com.example.arbackend.entity.Course;
import com.example.arbackend.repository.CourseRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @Mock
    private CourseRepository repo;

    @InjectMocks
    private CourseService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        Course course = new Course();
        course.setTitle("Test");
        when(repo.save(any(Course.class))).thenReturn(course);

        Course created = service.create(course);
        assertNotNull(created);
        verify(repo).save(course);
    }

    // more tests…
}
```

---

## 9️⃣  Performance & Security Notes

| Issue | Mitigation |
|-------|------------|
| **Stateless authentication** – use JWTs and API keys with short expiry. | Implement refresh tokens and rotating keys. |
| **CORS** – enforce proper allowed origins. | `HttpSecurity.cors(cors -> cors.configurationSource(...))`. |
| **Rate‑limit** – per‑partner request throttling. | Use Bucket4j or Spring Cloud Gateway. |
| **SQL injection** – use Spring Data JPA (parameter binding). | |
| **Data privacy** – encrypt sensitive data at rest. | Use AES encryption on DB columns. |
| **Monitoring** – health endpoints, metrics. | `/actuator/health`, Prometheus exporter. |
| **Logging** – redact secrets. | Use MDC, custom filter to mask API secret. |
| **Scalability** – stateless microservice, horizontally scalable behind load balancer. | Docker Swarm/K8s deployment. |

---

## 10️⃣  Production Checklist

1. **Generate strong JWT key** (`JWT_SECRET`) – 256‑bit.
2. **Configure HTTPS** – TLS termination at ingress or API gateway.
3. **Set environment variables** for DB, JWT secret, key rotation window, etc.
4. **Enable JPA auditing** to auto‑populate `createdAt`, `updatedAt`.
5. **Apply Flyway / Liquibase** migrations for schema versioning.
6. **Configure Docker secrets** for JWT key, DB credentials.
7. **Add health checks** (`/actuator/health`) and metrics.
8. **Document API** – OpenAPI spec, usage guide for partner apps.
9. **Implement OAuth2 / OIDC** if partners need single‑sign‑on (optional).
10. **Set up CI/CD** pipeline – build, test, Docker image publish, deploy.

---

### Final Thoughts

- **Keep it simple** – use the existing Spring Boot + Spring Security stack; only add custom key‑validation logic.
- **Secure everything** – API keys + JWTs + stateless sessions.
- **Be observant** – expose metrics, log request counts, use monitoring dashboards.
- **Plan for growth** – design for horizontal scaling, database sharding if needed.

## Front End Section:

1. **A public front‑end** that shows a 3‑D course preview with **React‑Three‑Fiber (R3F)**, protected only by an *API key* that the organization pastes into the UI.
2. **A lightweight back‑office UI** (mounted under `/admin`) where the same organization can upload courses, view a list of existing courses, delete them, and rotate/manage the API key that will be used on the public front‑end.

The back‑office and public pages share the same API layer; the only difference is the UI and the presence/absence of authentication.

---

## 1. Project bootstrap

```bash
# Vite + React (you can use CRA if you prefer)
npm create vite@latest r3f-portal -- --template react
cd r3f-portal
npm i
```

Install the required libs:

```bash
npm i react-router-dom @react-three/fiber @react-three/drei @chakra-ui/react @emotion/react @emotion/styled framer-motion
npm i axios
npm i react-query   # optional but handy for data fetching
```

> **Why Chakra UI?**  
> It gives you a clean, accessible UI out‑of‑the‑box with a small bundle size – perfect for a quick demo.

---

## 2. Project structure

```
src/
  ├─ api/
  │   └─ client.ts          # axios instance + helpers
  ├─ auth/
  │   ├─ AuthContext.tsx    # API‑key provider
  │   └─ useAuth.ts
  ├─ components/
  │   ├─ ApiKeyPrompt.tsx   # public front‑end “login”
  │   ├─ CourseCard.tsx
  │   └─ CourseViewer.tsx   # R3F component
  ├─ pages/
  │   ├─ Home.tsx
  │   ├─ Admin/
  │   │   ├─ Dashboard.tsx
  │   │   ├─ CoursesList.tsx
  │   │   └─ CourseForm.tsx
  │   └─ NotFound.tsx
  ├─ router.tsx
  ├─ App.tsx
  └─ main.tsx
```

---

## 3. API client (`src/api/client.ts`)

```ts
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.VITE_API_URL || 'http://localhost:8000/api',
});

/**
 * Attach the API key (org‑token) to every request header.
 */
export const setApiKey = (key: string | null) => {
  if (key) {
    api.defaults.headers.common['x-api-key'] = key;
  } else {
    delete api.defaults.headers.common['x-api-key'];
  }
};

export default api;
```

> The API key will be stored in localStorage for persistence across reloads.  
> In a production‑grade app you would implement refresh tokens / secure storage, but for a demo localStorage is fine.

---

## 4. Auth context (`src/auth/AuthContext.tsx`)

```tsx
import React, { createContext, useEffect, useState } from 'react';
import { setApiKey } from '../api/client';

type AuthContextType = {
  apiKey: string | null;
  setApiKey: (key: string | null) => void;
};

export const AuthContext = createContext<AuthContextType>({
  apiKey: null,
  setApiKey: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [apiKey, setApiKeyState] = useState<string | null>(() => localStorage.getItem('apiKey'));

  useEffect(() => {
    setApiKey(apiKey);
    if (apiKey) {
      localStorage.setItem('apiKey', apiKey);
    } else {
      localStorage.removeItem('apiKey');
    }
  }, [apiKey]);

  return (
    <AuthContext.Provider value={{ apiKey, setApiKey: setApiKeyState }}>
      {children}
    </AuthContext.Provider>
  );
};
```

---

## 5. Hook to use the auth context (`src/auth/useAuth.ts`)

```ts
import { useContext } from 'react';
import { AuthContext } from './AuthContext';

export const useAuth = () => useContext(AuthContext);
```

---

## 6. Public front‑end

### 6.1  `src/components/ApiKeyPrompt.tsx`

```tsx
import { useState } from 'react';
import { useAuth } from '../auth/useAuth';
import { Box, Input, Button, Heading, Text } from '@chakra-ui/react';

const ApiKeyPrompt: React.FC = () => {
  const { setApiKey } = useAuth();
  const [key, setKey] = useState('');

  const handleSubmit = () => setApiKey(key.trim() || null);

  return (
    <Box textAlign="center" mt={12}>
      <Heading mb={4}>Enter your organisation API key</Heading>
      <Input
        placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
        value={key}
        onChange={e => setKey(e.target.value)}
        size="lg"
        mb={4}
      />
      <Button onClick={handleSubmit} colorScheme="teal">
        Save
      </Button>
      <Text mt={2} color="gray.500">
        (You only need this once – it is stored locally in your browser)
      </Text>
    </Box>
  );
};

export default ApiKeyPrompt;
```

### 6.2  Course list (`src/components/CourseCard.tsx`)

```tsx
import { Box, Text, Image, LinkBox, LinkOverlay } from '@chakra-ui/react';
import { Link } from 'react-router-dom';

export type Course = {
  id: string;
  title: string;
  thumbnailUrl: string;
};

const CourseCard: React.FC<{ course: Course }> = ({ course }) => (
  <LinkBox
    p={4}
    borderWidth="1px"
    borderRadius="lg"
    overflow="hidden"
    _hover={{ shadow: 'md' }}
  >
    <Image src={course.thumbnailUrl} alt={course.title} objectFit="cover" h="150px" mb={2} />
    <LinkOverlay as={Link} to={`/course/${course.id}`}>
      <Text fontWeight="bold">{course.title}</Text>
    </LinkOverlay>
  </LinkBox>
);

export default CourseCard;
```

### 6.3  3‑D viewer (`src/components/CourseViewer.tsx`)

```tsx
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, Box } from '@react-three/drei';
import { useRef } from 'react';

const CourseViewer: React.FC = () => {
  const mesh = useRef<THREE.Mesh>(null!);

  // Spin the object
  useFrame(() => (mesh.current.rotation.y += 0.01));

  return (
    <Canvas style={{ height: '400px' }}>
      <ambientLight intensity={0.5} />
      <directionalLight position={[0, 10, 5]} intensity={1} />
      <mesh ref={mesh}>
        <Box args={[2, 2, 2]} />
        <meshStandardMaterial attach="material" color="royalblue" />
      </mesh>
      <OrbitControls />
    </Canvas>
  );
};

export default CourseViewer;
```

> Replace the simple box with a real **GLTF** model by importing it via `useGLTF` from `drei` and rendering it. For demo purposes, a spinning cube is enough.

### 6.4  `src/pages/Home.tsx`

```tsx
import { useEffect, useState } from 'react';
import { Box, SimpleGrid, Heading, Spinner } from '@chakra-ui/react';
import api from '../api/client';
import CourseCard, { Course } from '../components/CourseCard';
import CourseViewer from '../components/CourseViewer';
import ApiKeyPrompt from '../components/ApiKeyPrompt';
import { useAuth } from '../auth/useAuth';

const Home: React.FC = () => {
  const { apiKey } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!apiKey) return;
    setLoading(true);
    api
      .get<Course[]>('/courses')
      .then(res => setCourses(res.data))
      .finally(() => setLoading(false));
  }, [apiKey]);

  if (!apiKey) return <ApiKeyPrompt />;

  return (
    <Box p={4}>
      <Heading mb={6}>Course Catalog</Heading>

      <CourseViewer /> {/* 3‑D preview – you could add a dropdown to pick a model */}

      {loading ? (
        <Spinner size="xl" />
      ) : (
        <SimpleGrid columns={[1, 2, 3]} spacing={4} mt={6}>
          {courses.map(course => (
            <CourseCard key={course.id} course={course} />
          ))}
        </SimpleGrid>
      )}
    </Box>
  );
};

export default Home;
```

> **Tip:** If you want the 3‑D viewer to show the currently selected course, keep a `selectedCourse` state and pass a `modelUrl` prop to `CourseViewer`. The viewer can then load that URL with `useGLTF`.

---

## 7. Back‑office UI

All back‑office routes are prefixed with `/admin`. They are protected by the same API key (you can add a very light guard if you want). For a quick demo we’ll just reuse the same `ApiKeyPrompt` component.

### 7.1  Dashboard (`src/pages/Admin/Dashboard.tsx`)

```tsx
import { Box, Heading, Button, Stack } from '@chakra-ui/react';
import { Link } from 'react-router-dom';
import ApiKeyPrompt from '../../components/ApiKeyPrompt';
import { useAuth } from '../../auth/useAuth';

const Dashboard: React.FC = () => {
  const { apiKey } = useAuth();

  if (!apiKey) return <ApiKeyPrompt />;

  return (
    <Box p={4}>
      <Heading mb={6}>Organisation Back‑office</Heading>
      <Stack spacing={4}>
        <Button as={Link} to="courses" colorScheme="teal">
          Manage Courses
        </Button>
        <Button as={Link} to="api-keys" colorScheme="purple">
          API Key Management
        </Button>
      </Stack>
    </Box>
  );
};

export default Dashboard;
```

### 7.2  Course list (`src/pages/Admin/CoursesList.tsx`)

```tsx
import { useEffect, useState } from 'react';
import {
  Box,
  Heading,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  Button,
  IconButton,
  useToast,
} from '@chakra-ui/react';
import { DeleteIcon } from '@chakra-ui/icons';
import api from '../../api/client';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/useAuth';

type Course = {
  id: string;
  title: string;
  description: string;
};

const CoursesList: React.FC = () => {
  const { apiKey } = useAuth();
  const toast = useToast();
  const [courses, setCourses] = useState<Course[]>([]);
  const navigate = useNavigate();

  const fetch = () => {
    api.get<Course[]>('/courses').then(res => setCourses(res.data));
  };

  useEffect(() => {
    if (!apiKey) return;
    fetch();
  }, [apiKey]);

  const deleteCourse = async (id: string) => {
    if (!window.confirm('Delete this course?')) return;
    try {
      await api.delete(`/courses/${id}`);
      toast({ title: 'Deleted', status: 'success', duration: 2000 });
      fetch();
    } catch (e) {
      toast({ title: 'Error', status: 'error', duration: 2000 });
    }
  };

  if (!apiKey) return null; // already guarded

  return (
    <Box p={4}>
      <Heading mb={4}>Courses</Heading>
      <Button mb={4} colorScheme="teal" onClick={() => navigate('new')}>
        Add New Course
      </Button>
      <Table variant="simple">
        <Thead>
          <Tr>
            <Th>Title</Th>
            <Th>Description</Th>
            <Th>Actions</Th>
          </Tr>
        </Thead>
        <Tbody>
          {courses.map(c => (
            <Tr key={c.id}>
              <Td>{c.title}</Td>
              <Td>{c.description}</Td>
              <Td>
                <IconButton
                  aria-label="Delete"
                  icon={<DeleteIcon />}
                  colorScheme="red"
                  onClick={() => deleteCourse(c.id)}
                />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
    </Box>
  );
};

export default CoursesList;
```

### 7.3  Course form (`src/pages/Admin/CourseForm.tsx`)

```tsx
import { useState, useEffect } from 'react';
import {
  Box,
  Heading,
  FormControl,
  FormLabel,
  Input,
  Textarea,
  Button,
  useToast,
} from '@chakra-ui/react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/client';
import { useAuth } from '../../auth/useAuth';

const CourseForm: React.FC = () => {
  const { id } = useParams(); // null means “new”
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const toast = useToast();
  const navigate = useNavigate();
  const { apiKey } = useAuth();

  useEffect(() => {
    if (id) {
      api.get(`/courses/${id}`).then(res => {
        const c = res.data;
        setTitle(c.title);
        setDescription(c.description);
        setThumbnailUrl(c.thumbnailUrl);
      });
    }
  }, [id]);

  const handleSave = async () => {
    const payload = { title, description, thumbnailUrl };
    try {
      if (id) {
        await api.put(`/courses/${id}`, payload);
        toast({ title: 'Updated', status: 'success' });
      } else {
        await api.post('/courses', payload);
        toast({ title: 'Created', status: 'success' });
      }
      navigate('../');
    } catch (e) {
      toast({ title: 'Error', status: 'error' });
    }
  };

  if (!apiKey) return null;

  return (
    <Box p={4}>
      <Heading mb={4}>{id ? 'Edit Course' : 'New Course'}</Heading>
      <FormControl mb={4} isRequired>
        <FormLabel>Title</FormLabel>
        <Input value={title} onChange={e => setTitle(e.target.value)} />
      </FormControl>
      <FormControl mb={4} isRequired>
        <FormLabel>Description</FormLabel>
        <Textarea value={description} onChange={e => setDescription(e.target.value)} />
      </FormControl>
      <FormControl mb={4}>
        <FormLabel>Thumbnail URL</FormLabel>
        <Input value={thumbnailUrl} onChange={e => setThumbnailUrl(e.target.value)} />
      </FormControl>
      <Button colorScheme="teal" onClick={handleSave}>
        Save
      </Button>
    </Box>
  );
};

export default CourseForm;
```

> **Tip:** If you want to upload a GLTF file you could add a file input, store it in an S3‑compatible bucket, and store the public URL in the course record.

### 7.4  API‑key management

A very basic example: list existing keys, revoke and create new ones. Assuming your backend exposes `/api-keys` endpoints:

```tsx
import { Box, Heading, Table, Thead, Tbody, Tr, Th, Td, IconButton } from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/useAuth';
import api from '../../api/client';
import { DeleteIcon, AddIcon } from '@chakra-ui/icons';
import { useEffect, useState } from 'react';
import { useToast } from '@chakra-ui/react';

const ApiKeysPage: React.FC = () => {
  const { apiKey } = useAuth();
  const [keys, setKeys] = useState<string[]>([]);
  const toast = useToast();

  const fetch = () => api.get<string[]>('/api-keys').then(res => setKeys(res.data));

  useEffect(() => {
    if (!apiKey) return;
    fetch();
  }, [apiKey]);

  const revoke = async (key: string) => {
    if (!window.confirm('Revoke this key?')) return;
    await api.delete(`/api-keys/${key}`);
    fetch();
    toast({ title: 'Revoke', status: 'info', duration: 2000 });
  };

  const create = async () => {
    const newKey = await api.post('/api-keys', {});
    setKeys([...keys, newKey.data.key]);
    toast({ title: 'New key created', status: 'success' });
  };

  if (!apiKey) return null;

  return (
    <Box p={4}>
      <Heading mb={4}>API Keys</Heading>
      <Button mb={4} onClick={create} colorScheme="purple">
        Create New Key
      </Button>
      <Table variant="simple">
        <Thead>
          <Tr>
            <Th>Key</Th>
            <Th>Actions</Th>
          </Tr>
        </Thead>
        <Tbody>
          {keys.map(k => (
            <Tr key={k}>
              <Td>{k}</Td>
              <Td>
                <IconButton
                  aria-label="Revoke"
                  icon={<DeleteIcon />}
                  colorScheme="red"
                  onClick={() => revoke(k)}
                />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
    </Box>
  );
};

export default ApiKeysPage;
```

---

## 8. Routing (`src/router.tsx`)

```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ChakraProvider } from '@chakra-ui/react';
import Home from './pages/Home';
import Dashboard from './pages/Admin/Dashboard';
import CoursesList from './pages/Admin/CoursesList';
import CourseForm from './pages/Admin/CourseForm';
import ApiKeysPage from './pages/Admin/ApiKeysPage';

const AppRouter: React.FC = () => (
  <BrowserRouter>
    <ChakraProvider>
      <Routes>
        <Route path="/" element={<Home />} />

        {/* Admin subtree */}
        <Route path="admin" element={<Dashboard />}>
          <Route path="courses" element={<CoursesList />} />
          <Route path="courses/new" element={<CourseForm />} />
          <Route path="courses/:id" element={<CourseForm />} />
          <Route path="api-keys" element={<ApiKeysPage />} />
        </Route>
      </Routes>
    </ChakraProvider>
  </BrowserRouter>
);

export default AppRouter;
```

> This simple nesting keeps the back‑office UI self‑contained and only re‑uses the API‑key check.

---

## 9. Build and run

```bash
# 1️⃣ Install dependencies
npm install @chakra-ui/react @emotion/react @emotion/styled framer-motion
npm install @react-three/fiber @react-three/drei
npm install axios react-router-dom

# 2️⃣ Start the dev server
npm run dev
```

Open <http://localhost:5173> (or the port Vite reports). You’ll see:

* The **Home** page asks for the organisation’s API key.
* After entering the key, a spinner shows while the list of courses is fetched, and a 3‑D viewer displays a rotating cube.
* Clicking a course title navigates to a page where you can view details (for the 3‑D part, you can wire up a model URL per course).

If you open <http://localhost:5173/admin>, the same key prompt appears. Once the key is accepted, you get two buttons: *Manage Courses* and *API Key Management*.  
From there you can add, edit, or delete courses, and see their thumbnails in the public catalog.

---

## 10. Next steps & polishing

1. **GLTF loading** – Use `useGLTF` from `drei` and replace the placeholder cube with the actual course assets.
2. **File uploads** – Hook the form to an S3 bucket or your backend file store so you can upload 3‑D files directly.
3. **Security** – Add a tiny auth guard in the back‑office that redirects to the key prompt if the key is missing or expired.
4. **Styling** – The Chakra UI components already give a clean, responsive look. Adjust spacing, colors, and typography as you wish.
5. **Testing** – Write unit tests with `vitest` or integration tests with Cypress.
6. **Deployment** – Build (`npm run build`) and serve via Vercel, Netlify, or your own Node server.

---

### What this solution gives you

* **A clear separation** between the *public catalog* and the *organisation back‑office*.
* **Full usage of the same API key** for both parts – no need for a separate user login.
* **A 3‑D viewer** that you can hook up to real GLTF models.
* **CRUD** for courses, including thumbnails that populate the public catalog.
* **Simple UI** built with Chakra‑UI that’s responsive and accessible.