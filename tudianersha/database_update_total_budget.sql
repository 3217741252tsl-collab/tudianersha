-- 添加总预算字段到travel_projects表
ALTER TABLE travel_projects 
ADD COLUMN IF NOT EXISTS total_budget DECIMAL(10,2) COMMENT '总预算';
