# Examme - Smart Examination Platform 🎓

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-green?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)
![Gemini AI](https://img.shields.io/badge/Gemini_AI-Integrated-purple?style=for-the-badge&logo=google-gemini)

## 📝 Project Overview

**Examme** is a production-grade examination platform designed to streamline the creation, management, and assessment of academic tests. Leveraging **Google's Gemini AI**, the platform allows teachers to generate high-quality quiz questions directly from lecture notes and documents.

### Core Features
- 🤖 **AI Question Generation**: Automatically extract questions from PDF/DOCX/TXT files using Gemini AI.
- 👥 **Group Management**: Teachers can create study groups and invite students via email.
- ⏱️ **Real-time Quizzing**: Students can take timed exams with instant results.
- 📊 **Detailed Analytics**: Comprehensive reporting for both teachers and students.
- 🔒 **Secure Auth**: JWT-based authentication with Refresh Token rotation.

---

## 📂 Project Structure

```text
examme/
├── .github/workflows/       # CI/CD Pipelines
├── docs/                    # Detailed Documentation
├── src/
│   ├── main/
│   │   ├── java/com/examme/ # Backend Logic
│   │   └── resources/       # Configuration
│   └── test/                # Unit & Integration Tests
├── Dockerfile               # Containerization
├── docker-compose.yml       # Orchestration
└── pom.xml                  # Dependency Management
```

---

## 🔗 Quick Links

Explore the detailed documentation in the `/docs` folder:

| Document | Description |
| :--- | :--- |
| 🏗️ [Architecture](docs/architecture.md) | System design and user workflows. |
| 🗄️ [Database Schema](docs/database.md) | Entity Relationship Diagram and table structures. |
| 🔌 [API Specification](docs/api-spec.md) | Full list of REST endpoints and payloads. |
| ✨ [Features & Logic](docs/features.md) | Deep dive into business logic and roles. |
| 🚀 [Deployment Guide](docs/deployment.md) | Dockerization and CI/CD instructions. |

---

## 🛠️ Installation & Setup

### Prerequisites
- JDK 17
- Maven 3.8+
- Docker & Docker Compose
- Gemini API Key

### Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/examme.git
   cd examme
   ```

2. **Configure Environment Variables**:
   Create a `.env` file in the root directory:
   ```env
   DB_PASSWORD=your_mysql_password
   JWT_SECRET=your_super_secret_key
   GEMINI_API_KEY=your_gemini_api_key
   ```

3. **Run with Docker Compose**:
   ```bash
   docker-compose up --build
   ```

4. **Access the API**:
   - Backend: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

---
[Back to Top](#examme---smart-examination-platform-)
