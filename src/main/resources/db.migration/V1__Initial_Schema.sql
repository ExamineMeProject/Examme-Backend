-- =========================================
-- USERS
-- =========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_role (role)
    );

-- =========================================
-- QUIZZES
-- =========================================
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL,
    total_questions INT NOT NULL,
    total_time INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_teacher_id (teacher_id),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_active (is_active)
    );

-- =========================================
-- QUESTIONS
-- =========================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL,
    marks INT NOT NULL,
    order_num INT NOT NULL,
    correct_answer TEXT,
    explanation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,

    INDEX idx_quiz_id (quiz_id),
    INDEX idx_difficulty (difficulty_level)
    );

-- =========================================
-- QUESTION OPTIONS
-- =========================================
CREATE TABLE IF NOT EXISTS question_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    option_number INT NOT NULL,
    is_correct BOOLEAN NOT NULL,

    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    INDEX idx_question_id (question_id),
    INDEX idx_correct (is_correct)
    );

-- =========================================
-- STUDENT QUIZ ATTEMPTS
-- =========================================
CREATE TABLE IF NOT EXISTS student_quiz_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    total_score INT,
    max_score INT,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    selected_question_ids TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,

    INDEX idx_student_id (student_id),
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_is_completed (is_completed)
    );

-- =========================================
-- STUDENT ANSWERS
-- =========================================
CREATE TABLE IF NOT EXISTS student_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    is_correct BOOLEAN,
    marks_obtained INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (attempt_id) REFERENCES student_quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    UNIQUE KEY unique_attempt_question (attempt_id, question_id),

    INDEX idx_attempt_id (attempt_id),
    INDEX idx_question_id (question_id)
    );

-- =========================================
-- FILE UPLOADS
-- =========================================
CREATE TABLE IF NOT EXISTS file_uploads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    extracted_content LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_teacher_id (teacher_id)
    );

-- =========================================
-- REFRESH TOKENS
-- =========================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(768) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date)
    );

-- =========================================
-- STUDY GROUPS
-- =========================================
CREATE TABLE IF NOT EXISTS study_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    teacher_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_teacher_id (teacher_id)
    );

-- =========================================
-- GROUP STUDENTS
-- =========================================
CREATE TABLE IF NOT EXISTS group_students (
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,

    PRIMARY KEY (group_id, student_id),

    FOREIGN KEY (group_id) REFERENCES study_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- =========================================
-- GROUP INVITATIONS
-- =========================================
CREATE TABLE IF NOT EXISTS group_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES study_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_group_id (group_id),
    INDEX idx_student_id (student_id)
    );

-- =========================================
-- NOTIFICATIONS
-- =========================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    message VARCHAR(2000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    invitation_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
    );

-- =========================================
-- EXAM PACKAGES
-- =========================================
CREATE TABLE IF NOT EXISTS exam_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    group_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    total_questions INT NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES study_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_group_id (group_id),
    INDEX idx_teacher_id (teacher_id)
    );

-- =========================================
-- QUIZ QUESTIONS
-- =========================================
CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_package_id BIGINT NOT NULL,
    question_number INT NOT NULL,
    question_text VARCHAR(4000) NOT NULL,
    option_a VARCHAR(2000) NOT NULL,
    option_b VARCHAR(2000) NOT NULL,
    option_c VARCHAR(2000) NOT NULL,
    option_d VARCHAR(2000) NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,

    FOREIGN KEY (exam_package_id) REFERENCES exam_packages(id) ON DELETE CASCADE,

    INDEX idx_exam_package_id (exam_package_id)
    );

-- =========================================
-- QUIZ SESSIONS
-- =========================================
CREATE TABLE IF NOT EXISTS quiz_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    exam_package_id BIGINT NOT NULL,
    from_question INT NOT NULL,
    to_question INT NOT NULL,
    selected_count INT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',

    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_package_id) REFERENCES exam_packages(id) ON DELETE CASCADE,

    INDEX idx_student_id (student_id),
    INDEX idx_exam_package_id (exam_package_id)
    );

-- =========================================
-- QUIZ SESSION QUESTIONS
-- =========================================
CREATE TABLE IF NOT EXISTS quiz_session_questions (
    session_id BIGINT NOT NULL,
    quiz_question_id BIGINT NOT NULL,

    PRIMARY KEY (session_id, quiz_question_id),

    FOREIGN KEY (session_id) REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
    );

-- =========================================
-- QUIZ ANSWERS
-- =========================================
CREATE TABLE IF NOT EXISTS quiz_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    quiz_question_id BIGINT NOT NULL,
    selected_answer VARCHAR(1),
    is_correct BOOLEAN NOT NULL,

    FOREIGN KEY (session_id) REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE,

    INDEX idx_session_id (session_id)
    );