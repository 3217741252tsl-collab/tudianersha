-- 增加cover_image_url字段长度以支持多张图片URL
USE tudianersha;

ALTER TABLE ai_generated_routes 
MODIFY COLUMN cover_image_url TEXT COMMENT '路线封面图片URL（多张图片用逗号分隔）';
