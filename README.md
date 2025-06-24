
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Unlicense License][license-shield]][license-url]

# 🧠 Habit-Tracker-API

A backend REST API for tracking personal habits: create daily/weekly habits, perform check-ins, and track streaks. Built with **Java 21** and **Spring Boot**, it’s designed to simulate real-world backend development workflows — including CI, Docker-based environments, and API documentation.

## 🏗️ Table of Contents

1. [About The Project](#about-the-project)  
2. [Built With](#built-with)  
3. [Getting Started](#getting-started)  
    - [Prerequisites](#prerequisites)  
    - [Installation](#installation)  
4. [Contributing](#contributing)  
5. [License](#license)  
6. [Contact](#contact)  

## 🔍 About The Project

This API enables users to:

- Register and authenticate (JWT-based)  
- Create, view, update, and delete habits (daily or weekly)  
- Perform check-ins for habits  
- View current streaks and progress history  

It’s a backend-first solution with a clean domain model (`User`, `Habit`, `CheckIn`, `Streak`), and is developed using best practices like layered architecture, database migrations, and Docker for local environments.

## 🛠️ Built With

- **Java** 21+  
- **Spring Boot** (Web, Data, Security, Validation)  
- **PostgreSQL** (via Docker)  
- **Spring Security + JWT**  
- **Flyway** (database migrations)  
- **Swagger/OpenAPI** (API docs)  
- **JUnit + Mockito** (tests)  
- **Docker & Docker Compose**  
- **GitHub Actions** (CI for tests & linting)

## 🚀 Getting Started

### 🔌 Prerequisites

Make sure you have installed:

- [ ] Java 21+  
- [ ] Docker & Docker Compose  
- [ ] Gradle (or use `./gradlew` wrapper)

### ⚙️ Installation

Clone the repo and start services:

```bash
git clone https://github.com/<your-org>/habit-tracker-api.git
cd habit-tracker-api
docker-compose up --build
```

Swagger UI will be available at:  
```
http://localhost:8080/swagger-ui.html
```

## 🤝 Contributing

Contributions welcome! Please follow:

1. Fork the project  
2. Create a feature branch (`feature/your-feature`)  
3. Commit your changes  
4. Push to your branch  
5. Open a Pull Request

Ensure all tests pass and adhere to the branch protection rules on `main`.

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for details.

## 📬 Contact

- **Hyusein** – *Developer* – hl.dev.acc@gmail.com  
- **Shenol** – *Developer* – shenolshengov41@gmail.com

Project link: [https://github.com/hyuseinleshov/habit-tracker-api](https://github.com/hyuseinleshov/habit-tracker-api)

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[contributors-url]: https://github.com/hyuseinleshov/habit-tracker-api/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[forks-url]: https://github.com/hyuseinleshov/habit-tracker-api/network/members
[stars-shield]: https://img.shields.io/github/stars/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[stars-url]: https://github.com/hyuseinleshov/habit-tracker-api/stargazers
[issues-shield]: https://img.shields.io/github/issues/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[issues-url]: https://github.com/hyuseinleshov/habit-tracker-api/issues
[license-shield]: https://img.shields.io/github/license/hyuseinleshov/habit-tracker-api.svg?style=for-the-badge
[license-url]: https://github.com/hyuseinleshov/habit-tracker-api/blob/master/LICENSE.txt
