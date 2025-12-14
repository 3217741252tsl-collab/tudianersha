/**
 * 协作界面数据适配器
 * 处理新旧两种数据格式：
 * - 旧格式: activities是字符串数组 ["08:00-09:00 早餐：老莞城茶餐厅", ...]
 * - 新格式: activities是对象数组 [{text: "08:00-09:00 早餐：老莞城茶餐厅", poiInfo: {...}}, ...]
 */

/**
 * 从activity中提取景点名称
 */
function getAttractionNameFromActivity(activity) {
  let activityText = '';
  let poiInfo = null;
  
  if (typeof activity === 'object' && activity.text) {
    // 新的结构化数据
    activityText = activity.text;
    poiInfo = activity.poiInfo;
  } else if (typeof activity === 'string') {
    // 兼容旧的字符串格式
    activityText = activity;
  }
  
  // 如果有poiInfo，直接使用高德返回的名称
  if (poiInfo && poiInfo.name) {
    return poiInfo.name;
  }
  
  // 否则从文本中提取
  return extractAttractionName(activityText);
}

/**
 * 从activity中提取完整信息
 */
function parseActivity(activity) {
  let activityText = '';
  let poiInfo = null;
  
  if (typeof activity === 'object' && activity.text) {
    // 新的结构化数据
    activityText = activity.text;
    poiInfo = activity.poiInfo;
  } else if (typeof activity === 'string') {
    // 兼容旧的字符串格式
    activityText = activity;
  }
  
  // 解析时间和内容
  const timeMatch = activityText.match(/^(\d{2}:\d{2}-\d{2}:\d{2})\s*(.*)/);
  let timeRange = '';
  let content = activityText;
  
  if (timeMatch) {
    timeRange = timeMatch[1];
    content = timeMatch[2];
  }
  
  return {
    text: activityText,
    timeRange: timeRange,
    content: content,
    poiInfo: poiInfo
  };
}

/**
 * 判断是否为餐饮活动
 */
function isMealActivity(activityText) {
  return activityText.includes('早餐') || 
         activityText.includes('午餐') || 
         activityText.includes('晚餐') || 
         activityText.includes('餐饮');
}

/**
 * 判断是否为景点活动
 */
function isAttractionActivity(content) {
  return content.includes('景点：') || 
         content.includes('上午：') || 
         content.includes('下午：') || 
         content.includes('晚上：') ||
         (!isMealActivity(content));
}

console.log('[协作界面数据适配器] 已加载');
