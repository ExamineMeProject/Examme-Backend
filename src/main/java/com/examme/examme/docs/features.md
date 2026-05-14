# ✨ Features & Business Logic

## User Roles

### 🧑‍🏫 Teacher
- **Group Creator**: Can create study groups and manage student membership.
- **AI Orchestrator**: Uploads lecture materials and configures the AI to generate questions.
- **Analytics Viewer**: Accesses detailed performance metrics for all students in their groups.

### 🧑‍🎓 Student
- **Exam Taker**: Can join groups via invitation and take assigned exams.
- **Progress Tracker**: Views personal result history and performance trends.

### 🛡️ Admin
- **System Manager**: Can manage all users, groups, and system-wide resources.

---

## Core Logic: AI Question Generation

1. **File Parsing**: The system extracts text from PDF, DOCX, or TXT files using `FileProcessingUtil`.
2. **Prompt Engineering**: The extracted text is wrapped in a specific prompt designed to elicit structured JSON output from Gemini AI.
3. **Validation**: The backend validates the AI's JSON output against the `QuizQuestionResponseDto` schema.
4. **Persistence**: Questions are mapped to an `ExamPackage` and saved for future sessions.

## Core Logic: Quiz Evaluation

- **Normalization**: Student answers (e.g., "a", " A ") are normalized to uppercase characters before comparison.
- **Scoring**: Scores are calculated as a percentage of correct answers vs. total selected questions.
- **Session Locking**: Once a session is submitted, it is marked as `COMPLETED` and cannot be retaken.

---
[Return to README](../README.md) | [Back to Top](#features--business-logic)
