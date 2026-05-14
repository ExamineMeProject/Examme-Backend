# 🗄️ Database Schema

## Entity Relationship Diagram (ERD)

The following diagram defines the relationships between core entities in the Examme system.

```mermaid
erDiagram
    USER ||--o{ STUDY_GROUP : "teaches"
    USER ||--o{ GROUP_INVITATION : "receives"
    USER ||--o{ REFRESH_TOKEN : "owns"
    STUDY_GROUP ||--o{ USER : "has students"
    STUDY_GROUP ||--o{ EXAM_PACKAGE : "contains"
    EXAM_PACKAGE ||--o{ QUIZ_QUESTION : "has"
    EXAM_PACKAGE ||--o{ QUIZ_SESSION : "tracks"
    QUIZ_SESSION ||--o{ QUIZ_ANSWER : "contains"
    QUIZ_QUESTION ||--o{ QUIZ_ANSWER : "validates"

    USER {
        long id PK
        string email
        string password
        string fullName
        string role
    }
    STUDY_GROUP {
        long id PK
        string name
        long teacher_id FK
    }
    EXAM_PACKAGE {
        long id PK
        string title
        string difficulty
        long group_id FK
    }
    QUIZ_QUESTION {
        long id PK
        string questionText
        string optionA
        string optionB
        string optionC
        string optionD
        string correctAnswer
    }
    QUIZ_SESSION {
        long id PK
        long student_id FK
        string status
        timestamp finishedAt
    }
```

## Data Dictionary

### Core Tables

| Table | Purpose | Key Relationships |
| :--- | :--- | :--- |
| `users` | Stores credentials and roles. | Central to all activity. |
| `study_groups` | Groups created by teachers. | Linked to `users` (Teacher/Student). |
| `exam_packages` | Collections of questions for a group. | Linked to `study_groups`. |
| `quiz_questions` | Individual AI-generated questions. | Linked to `exam_packages`. |
| `quiz_sessions` | Tracks student attempts. | Linked to `users` and `exam_packages`. |

---
[Return to README](../README.md) | [Back to Top](#database-schema)
