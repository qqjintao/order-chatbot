-- 测试环境数据库初始化脚本 (H2)
-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    avatar VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    session_id VARCHAR(32) PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(50),
    channel VARCHAR(20) DEFAULT 'web',
    status TINYINT DEFAULT 1,
    title VARCHAR(200),
    agent_id BIGINT,
    close_reason VARCHAR(200),
    message_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE IF NOT EXISTS chat_message (
    message_id VARCHAR(32) PRIMARY KEY,
    session_id VARCHAR(32) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'text',
    intent VARCHAR(50),
    sentiment VARCHAR(20),
    rag_docs TEXT,
    tokens_used INT,
    response_time DOUBLE,
    feedback_rating TINYINT,
    feedback_comment VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工单表
CREATE TABLE IF NOT EXISTS ticket (
    ticket_no VARCHAR(32) PRIMARY KEY,
    session_id VARCHAR(32),
    ticket_type VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'P2',
    status VARCHAR(20) DEFAULT 'PENDING',
    business_no VARCHAR(50),
    customer_name VARCHAR(50),
    customer_phone VARCHAR(20),
    assignee_id BIGINT,
    assignee_name VARCHAR(50),
    resolution TEXT,
    resolution_type VARCHAR(50),
    sla_deadline TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolve_time TIMESTAMP
);

-- 工单流转记录表
CREATE TABLE IF NOT EXISTS ticket_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(32) NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    action VARCHAR(50),
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    note TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库分类表
CREATE TABLE IF NOT EXISTS knowledge_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_doc (
    doc_id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    category_id BIGINT,
    category_name VARCHAR(100),
    file_format VARCHAR(20),
    file_url VARCHAR(500),
    chunk_count INT DEFAULT 0,
    tags VARCHAR(500),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化默认用户
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lZqsNQaCNQqGqDZ2i', '管理员', 'admin', 1),
('agent', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lZqsNQaCNQqGqDZ2i', '客服坐席', 'agent', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lZqsNQaCNQqGqDZ2i', '普通用户', 'user', 1);