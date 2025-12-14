/**
 * 提取景点名称（与后端逻辑一致）
 * 从复杂的活动描述中提取干净的景点名称
 */
function extractAttractionName(content) {
  // 移除时间段
  content = content.replace(/^\d{2}:\d{2}-\d{2}:\d{2}\s*/, '');
  
  // 移除前缀
  content = content.replace(/^[早午晚]餐[:：\s]*/, '');
  content = content.replace(/^[上下]午[:：\s]*/, '');
  content = content.replace(/^晚上[:：\s]*/, '');
  content = content.replace(/^景点[:：\s]*/, '');
  
  // 1. 尝试提取单引号中的内容（最优先）
  const singleQuoteMatch = content.match(/'([^']+)'/);
  if (singleQuoteMatch) {
    return singleQuoteMatch[1].trim();
  }
  
  // 2. 尝试提取双引号中的内容
  const doubleQuoteMatch = content.match(/"([^"]+)"/);
  if (doubleQuoteMatch) {
    return doubleQuoteMatch[1].trim();
  }
  
  // 3. 提取【】中的内容
  const bracketMatch = content.match(/【([^】]+)】/);
  if (bracketMatch) {
    return bracketMatch[1].trim();
  }
  
  // 4. 移除常见的动作词
  let cleaned = content
    .replace(/^(?:步行至|乘坐.*?前往|参观|游览|前往|在.*?享用)\s*/, '')
    .replace(/(?:参观.*|游览.*|享用.*)$/, '')
    .trim();
  
  // 5. 提取第一个逗号、句号、冒号之前的内容
  const parts = cleaned.split(/[,，。：:]/);
  if (parts.length > 0 && parts[0].trim()) {
    cleaned = parts[0].trim();
  }
  
  // 6. 再次移除开头的动词
  cleaned = cleaned.replace(/^(参观|游览|前往|打卡|体验)\s*/, '');
  
  // 如果清理后的内容不为空且有变化，返回清理后的结果
  if (cleaned && cleaned !== content) {
    return cleaned.substring(0, 30).trim(); // 限制长度
  }
  
  // 否则返回原内容的前20个字符
  return content.substring(0, 20).trim();
}
