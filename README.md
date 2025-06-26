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
        </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

Our goal is:

"Habit Tracker API is a Spring Boot backend that helps users manage daily and weekly habits. Users can:
- Register/login with JWT authentication
- Create, read, update, delete habits
- Perform check‑ins per habit
- View current streaks and historic stats

This project is structured with clean architecture, Docker, CI pipelines, and full API documentation—all without a UI layer."

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- BUILT WITH -->
### Built With

* [![Java][java-shield]][java-url]
* [![Spring Boot][spring-boot-shield]][spring-boot-url]
* [![Gradle][gradle-shield]][gradle-url]

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

2. Build and run the application:

    ```bash
    ./gradlew bootRun
    ```

3. The API will be available at:

    ```
    http://localhost:8080/
    ```

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
