-- 添加预算JSON字段到ai_generated_routes表
ALTER TABLE ai_generated_routes 
ADD COLUMN IF NOT EXISTS budgets_json TEXT COMMENT '活动预算数据（JSON格式）';
