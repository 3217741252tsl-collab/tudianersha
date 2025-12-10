-- 添加封面图片URL字段到ai_generated_routes表
ALTER TABLE ai_generated_routes 
ADD COLUMN cover_image_url VARCHAR(500) COMMENT '路线封面图片URL';

-- 添加其他结构化字段（如果还没有）
ALTER TABLE ai_generated_routes 
ADD COLUMN IF NOT EXISTS route_title VARCHAR(255) COMMENT '路线名称',
ADD COLUMN IF NOT EXISTS route_tag VARCHAR(100) COMMENT '特色标签',
ADD COLUMN IF NOT EXISTS attractions_count INT COMMENT '景点数量',
ADD COLUMN IF NOT EXISTS restaurants_count INT COMMENT '餐厅数量',
ADD COLUMN IF NOT EXISTS transport_mode VARCHAR(100) COMMENT '交通方式',
ADD COLUMN IF NOT EXISTS total_budget INT COMMENT '总预算',
ADD COLUMN IF NOT EXISTS recommendation_score INT COMMENT '推荐指数',
ADD COLUMN IF NOT EXISTS daily_itinerary TEXT COMMENT '每日行程JSON';
