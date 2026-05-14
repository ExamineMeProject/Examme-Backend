# 🚀 Deployment & CI/CD

## Dockerization

The project is fully containerized using a multi-stage Docker build for efficiency and security.

### Dockerfile Breakdown
1. **Build Stage**: Uses `maven:3.9.6-eclipse-temurin-17` to compile the JAR and cache dependencies.
2. **Run Stage**: Uses `eclipse-temurin:17-jre-jammy` (JRE only) for a smaller, more secure runtime image.
3. **Security**: Runs under a non-root `spring` user.

### Orchestration
The `docker-compose.yml` file defines two services:
- **`mysql`**: Version 8.0, with persistent data volumes.
- **`backend`**: The Spring Boot application, configured to wait for the database healthcheck.

---

## 🛠️ CI/CD Pipelines (GitHub Actions)

### 🧪 Continuous Integration (`ci.yml`)
Triggered on every push to `main` or pull request.
- Spins up a MySQL service container.
- Sets up JDK 17.
- Runs `mvn clean package` (executing all unit and integration tests).

### 🚢 Continuous Deployment (`deploy.yml`)
Triggered on push to `main`.
- Builds the production JAR.
- (Optional) Publishes to a cloud provider or container registry.

---

## Environment Variables Configuration

Ensure the following secrets are set in your deployment environment:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_URL` | JDBC Connection String | `jdbc:mysql://mysql:3306/examme_db` |
| `DB_PASSWORD` | MySQL Root Password | `admin` |
| `JWT_SECRET` | Secret for token signing | REQUIRED |
| `GEMINI_API_KEY`| Google AI API Key | REQUIRED |

---
[Return to README](../README.md) | [Back to Top](#deployment--cicd)
