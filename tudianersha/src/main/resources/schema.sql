-- 数据库表结构初始化脚本

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- 项目参与表
CREATE TABLE IF NOT EXISTS project_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    join_time DATETIME NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- 行程项目表
CREATE TABLE IF NOT EXISTS travel_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    days INT NOT NULL,
    creator_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    current_route_id BIGINT,
    start_date DATE,
    end_date DATE
);

-- 行程参与者表
CREATE TABLE IF NOT EXISTS travel_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission VARCHAR(20) NOT NULL
);

-- 预算表
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    total_budget DECIMAL(10,2) NOT NULL,
    used_budget DECIMAL(10,2) NOT NULL,
    remaining_budget DECIMAL(10,2) NOT NULL
);

-- AI生成行程表
CREATE TABLE IF NOT EXISTS ai_generated_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    route_content TEXT,
    generated_time DATETIME NOT NULL,
    interest_tags VARCHAR(200)
);

-- 总体路线图表
CREATE TABLE IF NOT EXISTS overall_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    route_details TEXT,
    created_time DATETIME NOT NULL
);

-- 活动安排表
CREATE TABLE IF NOT EXISTS activity_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    activity_name VARCHAR(100) NOT NULL,
    activity_time DATETIME,
    location VARCHAR(100),
    budget DECIMAL(10,2),
    day_number INT
);

-- 行程会话表
CREATE TABLE IF NOT EXISTS travel_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT,
    message_time DATETIME NOT NULL,
    mentioned_user_id BIGINT
);

-- 需求参数表
CREATE TABLE IF NOT EXISTS requirement_parameters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    interest_tags VARCHAR(200),
    daily_budget_allocation DECIMAL(10,2),
    wishlist TEXT,
    dislike_list TEXT,
    budget_breakdown TEXT
);

-- 分享文档表
CREATE TABLE IF NOT EXISTS shared_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    document_url VARCHAR(200),
    format VARCHAR(20),
    generated_time DATETIME NOT NULL,
    share_link VARCHAR(200),
    creator_id BIGINT NOT NULL
);

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50),
    message TEXT,
    created_time DATETIME NOT NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_created_time (created_time)
);

-- 项目任务表
CREATE TABLE IF NOT EXISTS project_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    task_name VARCHAR(200) NOT NULL,
    assignee_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    image_urls TEXT,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_assignee_id (assignee_id)
);