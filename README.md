# AR Backend

A multi-tenant Spring Boot backend API for an Augmented Reality (AR) learning platform. This application provides a secure RESTful API for e-learning partners to manage courses, modules, assets, and users, with support for interactive AR/VR learning experiences delivered through a WebGL front-end.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Caching Strategy](#caching-strategy)
- [Security](#security)
- [Development](#development)
- [License](#license)

## Features

- **Multi-tenant Architecture**: Support for multiple organizations with isolated data
- **Course Management**: Create and manage courses with modules and sections
- **Student Management**: Handle student enrollment and progress tracking
- **API Key Authentication**: Secure API access with generated API keys
- **Role-based Access Control**: Different roles for organization admins, staff, and students
- **Caching**: Redis-based caching for improved performance
- **Message Queue**: RabbitMQ integration for asynchronous processing
- **Database Migration**: Flyway for database schema versioning
- **API Documentation**: OpenAPI/Swagger documentation

## Technology Stack

- **Backend Framework**: Spring Boot 3.5.9
- **Language**: Java 17
- **Database**: MariaDB 12.0
- **Cache**: Redis 8.4
- **Message Queue**: RabbitMQ 4.0
- **ORM**: Spring Data JPA with Hibernate
- **Migration Tool**: Flyway
- **Security**: Spring Security
- **API Documentation**: SpringDoc OpenAPI 2.8.0
- **Build Tool**: Maven
- **Containerization**: Docker Compose
- **Better Stack**: Cloud Logging provider

## Architecture

The application follows a layered architecture with the following components:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic
- **Repositories**: Handle data access using JPA
- **Entities**: Represent database tables
- **DTOs**: Data transfer objects for API communication
- **Filters**: Handle authentication and multi-tenancy
- **Better Stack**: Online Logging and metrics hosting

### Multi-tenancy

The application implements multi-tenancy using a tenant filter that extracts the organization ID from either:
1. API Key header (X-API-KEY) for student/AR app access
2. Security context for authenticated organization/staff users

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ar-backend
```

### 2. Start Infrastructure Services

Use Docker Compose to start the required infrastructure services:

```bash
docker-compose up -d
```

This will start:
- MariaDB (port 3306)
- Redis (port 6379)
- RabbitMQ (ports 5672, 15672)
- Adminer (port 8081) - Database management UI

### 3. Configure Application

Update `src/main/resources/application.yaml` if needed:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://mariadb:3306/ar_db
    username: maria
    password: mariapassword
```

### 4. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on port 8080.

### 5. Access API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Project Structure

```
src/main/java/org/lucas/arbackend/
├── ArBackendApplication.java          # Main application class
├── config/                            # Configuration classes
│   ├── CacheConfig.java              # Redis cache configuration
│   ├── SecurityConfig.java           # Security configuration
│   ├── TenantFilter.java             # Multi-tenant filter
│   └── ...
├── controller/                        # REST controllers
│   ├── AdminController.java          # Admin endpoints
│   ├── CourseController.java         # Course endpoints
│   ├── OrganisationController.java   # Organization endpoints
│   └── StudentController.java        # Student endpoints
├── dto/                              # Data Transfer Objects
│   ├── course/                       # Course-related DTOs
│   ├── organisation/                 # Organization-related DTOs
│   └── student/                      # Student-related DTOs
├── entity/                           # JPA entities
│   ├── course/                       # Course entities
│   ├── Organisation/                # Organization entities
│   └── student/                      # Student entities
├── repository/                       # JPA repositories
├── service/                          # Business logic
└── util/                             # Utility classes
```

## API Documentation

The API is documented using OpenAPI/Swagger and can be accessed at `/swagger-ui.html`.

### Main API Endpoints

#### Organizations
- `POST /api/v1/organisations/signup` - Create a new organization
- `GET /api/v1/organisations/details` - Get organization details
- `PUT /api/v1/organisations/profile` - Update organization profile
- `POST /api/v1/organisations/api-keys` - Generate API key

#### Courses
- `GET /api/v1/courses` - List courses (paginated)

#### Students
- `POST /api/v1/students/org/{orgId}/enroll` - Enroll student in course
- `GET /api/v1/students/org/{orgId}` - Get student list (paginated)

#### Admin
- `POST /api/v1/admin/staff/create` - Add staff member
- `POST /api/v1/admin/course/create` - Create course with modules
- `GET /api/v1/admin/staff/all` - Get all staff
- `PUT /api/v1/admin/staff/update` - Update staff member

## Database

The application uses MariaDB with the following main entities:

- **Organisation**: Stores organization details
- **Course**: Represents learning courses
- **Module**: Subdivisions of courses
- **Section**: Individual sections within modules
- **Student**: End-users of the platform
- **Staff**: Organization staff members
- **ApiKey**: API credentials for secure access

Database migrations are managed using Flyway and located in `src/main/resources/db/migration/`.

## Caching Strategy

The application uses Redis for caching frequently accessed data:

- **API Keys**: Cached for 30 minutes
- **User Lookups**: Cached for 1 hour
- **Subscriptions**: Cached for 6 hours

## Security

The application implements multiple security measures:

1. **API Key Authentication**: For student/AR app access
   - API keys are generated with a unique prefix
   - Keys are hashed before storage
   - Only the raw key is shown once during generation

2. **Basic Authentication**: For organization and staff access

3. **Role-based Access Control**:
   - ORG_ADMIN: Full organization management
   - COURSE_EDITOR: Course content management
   - STUDENT: Course access and enrollment

4. **Multi-tenant Isolation**: Each organization's data is isolated

## Development

### Running Tests

```bash
./mvnw test
```

### Code Style

The project uses Lombok to reduce boilerplate code.

### Database Management

Access the Adminer UI at `http://localhost:8081` to manage the database:
- Server: `mariadb`
- Username: `maria`
- Password: `mariapassword`
- Database: `ar_db`

### RabbitMQ Management

Access the RabbitMQ Management UI at `http://localhost:15672`:
- Username: `guest`
- Password: `guest`

## License

Proprietary - All rights reserved.
