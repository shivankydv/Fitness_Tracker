# 🏋️ Fitness Tracker

A full-stack fitness tracking web application built using **Java and Spring Boot** that allows users to manage their fitness activities, track progress, and set personal fitness goals.

🌐 **Live Demo:** https://fitness-tracker-nyp5.onrender.com

## 📌 Overview

Fitness Tracker is designed to help users monitor their fitness journey through a web-based platform. It provides user authentication, activity tracking, and goal management.

The application follows a Spring Boot MVC architecture, uses Thymeleaf for server-side HTML rendering, and stores application data in a MySQL-compatible database.

## ✨ Features

- **User Authentication:** User registration and login with Spring Security.
- **Activity Tracking:** Record and manage fitness activities.
- **Goal Management:** Create and track personal fitness goals.
- **Dashboard:** View fitness-related information in one place.
- **Database Integration:** Persistent data storage using MySQL and Spring Data JPA.
- **Server-Side Rendering:** Dynamic web pages using Thymeleaf.
- **Containerization:** Docker-based application packaging.
- **Cloud Deployment:** Publicly accessible application deployed on Render with TiDB Cloud as the database provider.

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java |
| Backend | Spring Boot |
| Frontend | HTML, CSS, Thymeleaf |
| Security | Spring Security |
| ORM | Hibernate / Spring Data JPA |
| Database | MySQL-compatible TiDB |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| Deployment | Render |
| Database Hosting | TiDB Cloud |
| Version Control | Git and GitHub |

## 🏗️ Architecture

The application follows a layered Spring Boot architecture.

```text
              User / Browser
                    |
                    v
             Thymeleaf UI
                    |
                    v
          Spring Boot Controllers
                    |
                    v
             Service Layer
                    |
                    v
          Repository Layer (JPA)
                    |
                    v
              TiDB Cloud
             (MySQL Compatible)
```

### Deployment Architecture

```text
          GitHub Repository
                  |
                  v
             Render
          (Docker Build)
                  |
                  v
        Spring Boot Application
                  |
                  v
             TiDB Cloud
          (Database Hosting)
```

## 🚀 Getting Started

Follow these instructions to run the project locally.

### Prerequisites

Make sure you have installed:

- Java 21 or later
- Maven
- MySQL 8.x (for local non-Docker setup)
- Docker Desktop (for Docker setup)
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/shivankydv/Fitness_Tracker.git
```

```bash
cd Fitness_Tracker
```

### 2. Configure Environment Variables

The application supports environment-based database configuration.

For a local MySQL setup, configure:

```properties
DB_URL=jdbc:mysql://localhost:3306/fitness_tracker
DB_USERNAME=root
DB_PASSWORD=your_password
```

For deployment, configure the corresponding TiDB Cloud JDBC URL and credentials in the hosting platform's environment variables.

**Never commit database passwords or other secrets to GitHub.**

### 3. Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Open the application:

```text
http://localhost:8080
```

## 🐳 Running with Docker

The project includes a Dockerfile and Docker Compose configuration.

Build and start the application:

```bash
docker compose up --build
```

Stop the containers:

```bash
docker compose down
```

To remove containers and the associated database volume:

```bash
docker compose down -v
```

**Note:** The `-v` option deletes the local Docker database volume and its stored data. Use it carefully.

## ☁️ Deployment

The application is deployed using:

- **Render:** Hosts the Dockerized Spring Boot application.
- **TiDB Cloud:** Provides managed MySQL-compatible database hosting.
- **GitHub:** Stores the source code and supports automatic deployment on commits.

### Live Application

https://fitness-tracker-nyp5.onrender.com

The application uses environment variables for database configuration, keeping deployment credentials outside the source code.

**Note:** Render's free instance may spin down after inactivity, so the initial request can take longer while the service starts.

## 📂 Project Structure

```text
Fitness_Tracker/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fitnesstracker/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── domain/
│   │   │       ├── dto/
│   │   │       ├── security/
│   │   │       └── config/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw
└── README.md
```

## 🔐 Security

- Spring Security for application authentication and authorization.
- Password hashing using BCrypt.
- Environment variables for sensitive database credentials.
- Database access through Spring Data JPA.

## 🎯 Learning Outcomes

Through this project, I gained practical experience with:

- Developing RESTful and MVC-based applications using Spring Boot.
- Implementing authentication and authorization using Spring Security.
- Integrating MySQL-compatible databases using Spring Data JPA.
- Creating dynamic web pages with Thymeleaf.
- Containerizing applications using Docker.
- Deploying Java applications to cloud hosting platforms.
- Configuring external cloud databases and environment variables.

## 👨‍💻 Author

**Shivank Yadav**

- GitHub: https://github.com/shivankydv
- Project Repository: https://github.com/shivankydv/Fitness_Tracker

---

⭐ If you find this project useful, consider giving it a star!
