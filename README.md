<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/hyuseinleshov/habit-tracker-api">
    <img src="images/logo.png" alt="Habit Tracker Logo" width="80" height="80">
  </a>

<h3 align="center">Habit Tracker API</h3>

  <p align="center">
    A backend REST API for tracking daily/weekly habits, check‑ins, and streaks.
    <br />
    <br />
    <a href="https://github.com/hyuseinleshov/habit-tracker-api/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    &middot;
    <a href="https://github.com/hyuseinleshov/habit-tracker-api/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a>
        <ul>
          <li><a href="#built-with">Built With</a></li>
        </ul>
    </li>
    <li><a href="#getting-started">Getting Started</a>
        <ul>
          <li><a href="#prerequisites">Prerequisites</a></li>
          <li><a href="#installation">Installation</a></li>
          <li><a href="#environment-variables">Environment Variables</a></li>
        </ul>
    </li>
    <li><a href="#api-documentation">API Documentation</a>
        <ul>
          <li><a href="#core-api-categories">Core API Categories</a></li>
          <li><a href="#example-endpoints">Example Endpoints</a></li>
        </ul>
    </li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#database-schema">Database Schema</a>
        <ul>
          <li><a href="#key-entities">Key Entities</a></li>
          <li><a href="#database-design-principles">Database Design Principles</a></li>
        </ul>
    </li>
    <li><a href="#security">Security</a>
        <ul>
          <li><a href="#authentication-flow">Authentication Flow</a></li>
          <li><a href="#secured-endpoints">Secured Endpoints</a></li>
          <li><a href="#token-management">Token Management</a></li>
        </ul>
    </li>
    <li><a href="#testing">Testing</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

Habit Tracker API is a Spring Boot backend that helps users manage daily and weekly habits. Users can:
- Register/login with JWT authentication
- Create, read, update, delete habits
- Perform check‑ins per habit
- View current streaks and historic stats

This project is structured with clean architecture, Docker, CI pipelines, and full API documentation—all without a UI layer.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- BUILT WITH -->
### Built With

* [![Java][java-shield]][java-url]
* [![Spring Boot][spring-boot-shield]][spring-boot-url]
* [![Gradle][gradle-shield]][gradle-url]
* [![PostgreSQL][postgresql-shield]][postgresql-url]
* [![Redis][redis-shield]][redis-url]
* [![Docker][docker-shield]][docker-url]
* [![JWT][jwt-shield]][jwt-url]
* [![JUnit5][junit5-shield]][junit5-url]
* [![Hibernate][hibernate-shield]][hibernate-url]
* [![Lombok][lombok-shield]][lombok-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

Follow these instructions to run the project locally.

### Prerequisites

* Java 21+
* Gradle (optional — you can use the included `./gradlew` wrapper)

### Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/hyuseinleshov/habit-tracker-api.git
    cd habit-tracker-api
    ```

2. Set up the required environment variables (see [Environment Variables](#environment-variables) section).

3. Start the required infrastructure services using Docker:

    ```bash
    docker-compose up -d
    ```

4. Build and run the application:

    ```bash
    ./gradlew bootRun
    ```

5. The API will be available at:

    ```
    http://localhost:8080/
    ```

### Environment Variables

The application uses environment variables for configuration. Here are the essential ones you need to set:

#### Required Environment Variables

| Variable | Description |
|----------|-------------|
| `POSTGRES_USER` | PostgreSQL database username |
| `POSTGRES_PASSWORD` | PostgreSQL database password |
| `POSTGRES_DB` | PostgreSQL database name |
| `JWT_SECRET` | Secret key for signing JWT tokens |

#### Configuration Example

Create a `.env` file in the project root directory:

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password
POSTGRES_DB=habittracker
JWT_SECRET=your-secret-key
```

> **Note**: The application has many configurable options with sensible defaults. For a complete list of configuration properties and their default values, refer to:
>
> - `application.yml` - Base configuration
> - `application-dev.yml` - Development profile configuration
> - `application-prod.yml` - Production profile configuration (if available)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- API DOCUMENTATION -->
## API Documentation

The Habit Tracker API follows RESTful principles and uses JSON for request/response payloads. Below are key endpoint categories with examples.

### Core API Categories

- **Authentication**: User registration, login, and token refresh
- **User Profile**: User information management
- **Habits**: CRUD operations for habits
- **Check-ins**: Recording and retrieving habit completions
- **Statistics**: Retrieving habit performance metrics

### Example Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login with existing credentials |
| POST | `/api/auth/refresh` | Refresh an expired JWT token |

#### User Profile

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/me` | Get the current user's profile |
| PUT | `/api/me` | Update the current user's profile |

> **Note**: The API evolves with the application. For complete and up-to-date API documentation:
>
> 1. Check the controller classes in the source code

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- PROJECT STRUCTURE -->
## Project Structure

The project follows a clean architecture approach with domain-driven design principles. The codebase is organized into logical modules based on business domains:

- **auth**: Authentication and authorization functionality
- **security**: Security configuration and JWT implementation
- **userprofile**: User profile management
- **habit**: Core habit tracking functionality
- **checkin**: Check-in recording and tracking
- **core**: Shared utilities and base components

Each module follows a consistent structure with controllers, services, repositories, and models as appropriate for its functionality.

> **Note**: For the most up-to-date and detailed project structure, please refer to the source code repository.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- DATABASE SCHEMA -->
## Database Schema

The application uses a relational database (PostgreSQL) for persistent data storage and Redis for caching and temporary data.

### Key Entities

- **Authentication Domain**: Users, roles, and permissions
- **User Profile Domain**: User preferences and settings
- **Habit Domain**: Habit definitions and configurations
- **Check-in Domain**: Records of habit completions

### Database Design Principles

- UUID primary keys for all entities
- Timestamp tracking (created_at) for all records
- Proper foreign key relationships between related entities
- Role-based access control through user-role associations

> **Note**: The database schema evolves with the application. For the most up-to-date schema details, refer to the entity classes in the source code or use database inspection tools.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- TESTING -->
## Testing

The project includes comprehensive tests:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test interactions between components
- **API Tests**: Test the REST API endpoints

To run the tests:

```bash
./gradlew test
```

For development, you can use the pre-configured test users:
- Regular user: `user@example.com` / `user123`
- Admin user: `admin@example.com` / `admin123`

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- SECURITY -->
## Security

The Habit Tracker API implements a comprehensive security system to protect user data and ensure secure access to the application.

### Authentication Flow

The application uses JWT (JSON Web Token) based authentication with the following flow:

1. **Registration**:
    - Users register with email, password, and timezone
    - Password is encrypted using BCrypt
    - User is assigned the USER role
    - A JWT access token and refresh token are generated and returned

2. **Login**:
    - Users authenticate with email and password
    - Upon successful authentication, a JWT access token and refresh token are generated and returned
    - Any existing refresh tokens for the user are revoked

3. **Token Refresh**:
    - When the access token expires, clients can use the refresh token to obtain a new access token
    - The used refresh token is revoked, and a new refresh token is generated
    - This implements a rotation scheme for refresh tokens, enhancing security

### Secured Endpoints

The API implements the following security measures for endpoints:

- Public endpoints (no authentication required):
    - `/api/auth/register` - User registration
    - `/api/auth/login` - User login
    - `/api/auth/refresh` - Token refresh

- All other endpoints require authentication with a valid JWT token

- Method-level security is enabled, allowing for fine-grained access control based on roles and permissions

### Token Management

The application uses a sophisticated token management system:

- **JWT Access Tokens**:
    - Contain user email as the subject
    - Include issuedAt, expiration, and notBefore claims
    - Signed with a secret key
    - Configurable expiration time

- **Refresh Tokens**:
    - Stored securely in the database
    - One-time use (revoked after use)
    - Associated with a specific user
    - Used to obtain new access tokens when the current one expires

- **Security Features**:
    - Stateless authentication (no session state on the server)
    - Token validation on each request
    - Comprehensive error handling for token validation failures
    - Protection against token reuse

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTRIBUTING -->
## Contributing

We welcome contributions! Follow these steps:

1. Fork the project
2. Create your branch (`feature/xyz`, `fix/abc`)
3. Commit your changes (`feat: add xyz feature`)
4. Push to your branch
5. Open a Pull Request

Adhere to our branch/commit conventions.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

- **Hyusein Leshov** – hl.dev.acc@gmail.com
- **Shenol Shengov** – shenolshengov41@gmail.com

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[contributors-url]: https://github.com/hyuseinleshov/habit-tracker-api/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[forks-url]: https://github.com/hyuseinleshov/habit-tracker-api/network/members
[stars-shield]: https://img.shields.io/github/stars/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[stars-url]: https://github.com/hyuseinleshov/habit-tracker-api/stargazers
[issues-shield]: https://img.shields.io/github/issues/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[issues-url]: https://github.com/hyuseinleshov/habit-tracker-api/issues
[license-shield]: https://img.shields.io/github/license/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[license-url]: https://github.com/hyuseinleshov/habit-tracker-api/blob/main/LICENSE
[java-shield]: https://img.shields.io/badge/Java-DE0A26?logo=openjdk&logoColor=white&style=for-the-badge
[java-url]: https://www.oracle.com/java/
[spring-boot-shield]: https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white&style=for-the-badge
[spring-boot-url]: https://spring.io/projects/spring-boot
[gradle-shield]: https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white&style=for-the-badge
[gradle-url]: https://gradle.org/
[postgresql-shield]: https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white&style=for-the-badge
[postgresql-url]: https://www.postgresql.org/
[redis-shield]: https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=for-the-badge
[redis-url]: https://redis.io/
[docker-shield]: https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=for-the-badge
[docker-url]: https://www.docker.com/
[jwt-shield]: https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white&style=for-the-badge
[jwt-url]: https://jwt.io/
[junit5-shield]: https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white&style=for-the-badge
[junit5-url]: https://junit.org/junit5/
[hibernate-shield]: https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=white&style=for-the-badge
[hibernate-url]: https://hibernate.org/
[lombok-shield]: https://img.shields.io/badge/Lombok-BC4521?logo=lombok&logoColor=white&style=for-the-badge
[lombok-url]: https://projectlombok.org/
