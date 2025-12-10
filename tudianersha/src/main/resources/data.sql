-- 初始化数据脚本（仅在数据不存在时插入）
-- 用户表初始化数据（密码已使用BCrypt加密）
-- 明文密码：admin123, user123, tsl123, qjq123, ysy123
INSERT IGNORE INTO users (username, email, password) VALUES ('admin', 'admin@example.com', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1T58B/C');
INSERT IGNORE INTO users (username, email, password) VALUES ('user1', 'user1@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQhf2.v/zH9xqB7A0H1YE8RlhO');
INSERT IGNORE INTO users (username, email, password) VALUES ('tsl', 'tsl@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQhf2.v/zH9xqB7A0H1YE8RlhO');
INSERT IGNORE INTO users (username, email, password) VALUES ('qjq', 'qjq@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQhf2.v/zH9xqB7A0H1YE8RlhO');
INSERT IGNORE INTO users (username, email, password) VALUES ('ysy', 'ysy@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQhf2.v/zH9xqB7A0H1YE8RlhO');