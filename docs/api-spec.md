# 🔌 API Specification

## Authentication

| Method | Endpoint | Description | Request Body | Status |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user. | `UserRegistrationDto` | 200 OK |
| `POST` | `/api/auth/login` | Login and get JWT. | `LoginRequestDto` | 200 OK |
| `POST` | `/api/auth/refresh` | Refresh access token. | `RefreshRequestDto` | 200 OK |

## Exam Packages (Teacher Only)

| Method | Endpoint | Description | Request Body | Status |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/exam-packages` | Create AI-generated exam. | `Multipart (File + Info)` | 201 Created |
| `GET` | `/api/exam-packages/{id}` | Get package details. | N/A | 200 OK |
| `DELETE` | `/api/exam-packages/{id}` | Delete package. | N/A | 204 No Content |

## Quiz (Student Only)

| Method | Endpoint | Description | Request Body | Status |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/quiz/start` | Start a quiz session. | `QuizStartRequestDto` | 200 OK |
| `POST` | `/api/quiz/{id}/submit`| Submit answers. | `QuizSubmitRequestDto` | 200 OK |

## Results & Analytics

| Method | Endpoint | Description | Role | Status |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/results/my` | View my history. | STUDENT | 200 OK |
| `GET` | `/api/results/group/{id}`| View group results. | TEACHER | 200 OK |

---
[Return to README](../README.md) | [Back to Top](#api-specification)
