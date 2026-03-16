-- Users table with profile details
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_public BOOLEAN DEFAULT TRUE,
    profile_pic VARCHAR(255) DEFAULT 'default-user-icon.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Main Session Record (Parent)
CREATE TABLE IF NOT EXISTS pomodoro_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    focus_duration INT NOT NULL,
    short_break_duration INT NOT NULL,
    long_break_duration INT NOT NULL,
    sessions_before_long_break INT NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED') NOT NULL,
    current_phase ENUM('FOCUS', 'SHORT_BREAK', 'LONG_BREAK') DEFAULT 'FOCUS',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Individual Intervals/Sessions (children)
-- This tracks every single Focus, Short Break, and Long Break specifically.
CREATE TABLE IF NOT EXISTS pomodoro_intervals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    interval_type ENUM('FOCUS', 'SHORT_BREAK', 'LONG_BREAK') NOT NULL,
    duration_minutes INT NOT NULL,
    sequence_order INT NOT NULL, -- To track the order (1st, 2nd, 3rd)
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES pomodoro_sessions(id) ON DELETE CASCADE
);

-- User Settings
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    default_focus_minutes INT DEFAULT 25,
    default_short_break_minutes INT DEFAULT 5,
    default_long_break_minutes INT DEFAULT 15,
    sessions_before_long_break INT DEFAULT 4,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
