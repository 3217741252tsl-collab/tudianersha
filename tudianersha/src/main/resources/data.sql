-- 初始化数据脚本（仅在数据不存在时插入）
-- 用户表初始化数据
INSERT IGNORE INTO users (username, email, password) VALUES ('admin', 'admin@example.com', 'admin123');
INSERT IGNORE INTO users (username, email, password) VALUES ('user1', 'user1@example.com', 'user123');
INSERT IGNORE INTO users (username, email, password) VALUES ('tsl', 'tsl@example.com', 'tsl123');
INSERT IGNORE INTO users (username, email, password) VALUES ('qjq', 'qjq@example.com', 'qjq123');
INSERT IGNORE INTO users (username, email, password) VALUES ('ysy', 'ysy@example.com', 'ysy123');