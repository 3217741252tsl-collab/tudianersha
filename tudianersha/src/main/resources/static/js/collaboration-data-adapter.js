/**
 * 协作界面数据适配器
 * 处理新旧两种数据格式：
 * - 旧格式: activities是字符串数组 ["08:00-09:00 景点：东莞博物馆", ...]
 * - 新格式: activities是对象数组 [{text: "东莞博物馆", poiInfo: {...}}, ...]
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
 * 从 activity 中提取完整信息
 */
function parseActivity(activity) {
  let activityText = '';
  let poiInfo = null;
  let duration = null;  // 游玩时间
  let ticket = 0;       // 门票价格
  
  if (typeof activity === 'object' && activity.text) {
    // 新的结构化数据
    activityText = activity.text;
    poiInfo = activity.poiInfo;
    duration = activity.duration || null;
    ticket = activity.ticket || 0;
    
    // 调试日志
    console.log('[parseActivity] 景点:', activityText, '| duration:', activity.duration, '| ticket:', activity.ticket);
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
    poiInfo: poiInfo,
    duration: duration,  // 游玩时间
    ticket: ticket       // 门票价格
  };
}

/**
 * 判断是否为景点活动（移除餐饮后，所有活动都视为景点）
 */
function isAttractionActivity(content) {
  // 所有活动都是景点
  return true;
}

console.log('[协作界面数据适配器] 已加载');
