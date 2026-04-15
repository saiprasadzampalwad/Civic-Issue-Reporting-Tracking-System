-- ============================================================
-- Civic Issue Reporting System — MySQL 8 Schema (No Email Features - Phone Auth)
-- Run: mysql -u root -p < schema.sql
-- ============================================================
SET SQL_MODE = '';
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS civic_issues_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE civic_issues_db;

DROP TABLE IF EXISTS Ratings;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS Issue_Photos;
DROP TABLE IF EXISTS Status_Updates;
DROP TABLE IF EXISTS Issues;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Departments;

CREATE TABLE Departments (
    department_id   INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO Departments (department_name) VALUES
    ('Roads & Infrastructure'),('Water & Sewage'),
    ('Electricity & Lighting'),('Sanitation & Waste'),('Parks & Recreation');

CREATE TABLE Users (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    role            ENUM('CITIZEN','MUNICIPAL_ADMIN','MAINTENANCE_CREW') NOT NULL DEFAULT 'CITIZEN',
    department_id   INT DEFAULT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until    DATETIME DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_dept FOREIGN KEY (department_id) REFERENCES Departments(department_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- No Password_Resets, no email, no verify_token/is_verified

CREATE TABLE Issues (
    issue_id               INT AUTO_INCREMENT PRIMARY KEY,
    citizen_id             INT          NOT NULL,
    category               VARCHAR(100) NOT NULL,
    gps_location           VARCHAR(100),
    description            TEXT,
    status                 ENUM('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    assigned_department_id INT DEFAULT NULL,
    assigned_crew_id       INT DEFAULT NULL,
    sla_deadline           DATETIME DEFAULT NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_citizen FOREIGN KEY (citizen_id)            REFERENCES Users(user_id)            ON DELETE CASCADE,
    CONSTRAINT fk_issue_dept    FOREIGN KEY (assigned_department_id) REFERENCES Departments(department_id) ON DELETE SET NULL,
    CONSTRAINT fk_issue_crew    FOREIGN KEY (assigned_crew_id)       REFERENCES Users(user_id)            ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Issue_Photos (
    photo_id  INT AUTO_INCREMENT PRIMARY KEY,
    issue_id  INT          NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_photo_issue FOREIGN KEY (issue_id) REFERENCES Issues(issue_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Status_Updates (
    update_id       INT AUTO_INCREMENT PRIMARY KEY,
    issue_id        INT         NOT NULL,
    updated_by      INT         NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status      VARCHAR(20) NOT NULL,
    notes           TEXT,
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_su_issue FOREIGN KEY (issue_id)   REFERENCES Issues(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_su_user  FOREIGN KEY (updated_by) REFERENCES Users(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id   INT  NOT NULL,
    user_id    INT  NOT NULL,
    comment    TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cmt_issue FOREIGN KEY (issue_id) REFERENCES Issues(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_cmt_user  FOREIGN KEY (user_id)  REFERENCES Users(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Ratings (
    rating_id  INT AUTO_INCREMENT PRIMARY KEY,
    issue_id   INT NOT NULL UNIQUE,
    citizen_id INT NOT NULL,
    stars      TINYINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    feedback   TEXT,
    rated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rat_issue   FOREIGN KEY (issue_id)   REFERENCES Issues(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_rat_citizen FOREIGN KEY (citizen_id) REFERENCES Users(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

CREATE OR REPLACE VIEW vw_issue_stats AS
SELECT
    COUNT(*)                                     AS total_issues,
    SUM(status='OPEN')                           AS open_count,
    SUM(status IN ('ASSIGNED','IN_PROGRESS'))    AS in_progress_count,
    SUM(status='RESOLVED')                       AS resolved_count,
    SUM(status='CLOSED')                         AS closed_count,
    ROUND(AVG(CASE WHEN status IN ('RESOLVED','CLOSED')
        THEN TIMESTAMPDIFF(HOUR,created_at,NOW()) END),1) AS avg_resolution_hours
FROM Issues;

SHOW TABLES;
SELECT 'Schema updated - Phone auth, no email features!' AS result;

