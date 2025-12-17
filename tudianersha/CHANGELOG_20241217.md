# 途点儿啥 - 更新日志 (2024-12-17)

## 版本概述
本次更新主要包含以下核心功能改进：

---

## 一、PDF导出功能优化

### 1.1 移除时间列
- PDF输出表格中删除了时间列，简化展示

### 1.2 添加完整交通信息
- 在景点之间添加详细的交通信息段
- 交通信息包含：交通方式、时长、距离、费用、目的地
- 支持分步骤展示详细导航信息（步行、地铁、公交）
- 修复emoji字符导致的PDF生成失败问题

### 1.3 涉及文件
- `src/main/java/com/tudianersha/service/ItineraryPdfService.java`

---

## 二、协作界面功能增强

### 2.1 景点信息展示优化
- 删除开放时间和门票信息卡片，替换为景点图片展示
- Kimi生成内容改为优先查找门票获取和开放时间信息

### 2.2 聊天@功能完善
- 禁止用户@自己
- 当被其他用户@时播放声音提醒（使用自定义mp3文件）
- 被@的消息显示蓝色高亮背景

### 2.3 附近景点推荐优化
- 确保附近景点推荐携带高德位置信息
- 添加景点时自动保存POI位置坐标

### 2.4 交通卡片详情修复
- 修复交通详情弹窗显示"未知"的问题
- 使用预处理变量确保距离/时长正确显示

### 2.5 拖拽排序功能修复
- 修复拖拽排序后出发地交通卡片消失问题
- 修复拖拽后目的地到第一个景点交通不更新问题
- 修复departureLocation为undefined的问题

### 2.6 涉及文件
- `src/main/resources/static/collaboration.html`
- `src/main/resources/static/js/collaboration-data-adapter.js`
- `src/main/java/com/tudianersha/controller/AmapTestController.java`

---

## 三、路线选择界面优化

### 3.1 交通信息展示
- 完善路线选择界面的交通信息展示
- 支持分步骤导航信息渲染

### 3.2 涉及文件
- `src/main/resources/static/route-selection.html`

---

## 四、项目创建界面

### 4.1 出发地功能
- 新增出发地位置坐标存储字段
- 支持从出发地到第一个景点的交通规划

### 4.2 涉及文件
- `src/main/resources/static/create-project.html`
- `src/main/java/com/tudianersha/entity/TravelProject.java`

---

## 五、新增服务模块

### 5.1 交通卡片服务
- 新增 `TransportCardController.java` - 交通卡片API接口
- 新增 `TransportCard.java` - 交通卡片实体
- 新增 `TransportCardRepository.java` - 交通卡片数据仓库
- 新增 `TransportCardService.java` - 交通卡片业务逻辑

### 5.2 高德地图路线规划服务
- 新增 `AmapDirectionService.java` - 高德路线规划API封装

### 5.3 POI搜索服务优化
- `AmapPoiService.java` - 增强POI搜索功能

### 5.4 AI路线生成服务优化
- `AiGeneratedRouteService.java` - 完善路线生成与交通衔接逻辑

---

## 六、静态资源

### 6.1 新增文件
- `src/main/resources/static/sounds/mention.mp3` - @提醒声音文件

---

## 文件变更统计

| 文件 | 新增行 | 删除行 |
|------|--------|--------|
| collaboration.html | +1819 | -多处 |
| AiGeneratedRouteService.java | +393 | -少量 |
| route-selection.html | +328 | -少量 |
| ItineraryPdfService.java | +204 | -少量 |
| create-project.html | +206 | -少量 |
| AmapPoiService.java | +49 | -少量 |
| TravelProject.java | +36 | - |
| collaboration-data-adapter.js | +36 | -少量 |
| AmapTestController.java | +35 | -少量 |

**总计：9个文件修改，2661行新增，445行删除**

---

## 新增文件列表
1. `TransportCardController.java`
2. `TransportCard.java`
3. `TransportCardRepository.java`
4. `TransportCardService.java`
5. `AmapDirectionService.java`
6. `sounds/mention.mp3`
7. `tudianersha.sql` (数据库脚本)
