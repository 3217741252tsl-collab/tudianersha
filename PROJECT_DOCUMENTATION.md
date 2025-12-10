# 途点儿啥旅游规划系统 - 完整项目文档

**版本**: 1.1.0  
**创建日期**: 2025-12-10  
**GitHub**: https://github.com/3217741252tsl-collab/tudianersha

---

## 📚 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [技术栈详解](#3-技术栈详解)
4. [数据库设计](#4-数据库设计)
5. [核心功能模块](#5-核心功能模块)
6. [API 接口文档](#6-api-接口文档)
7. [前端页面说明](#7-前端页面说明)
8. [第三方服务集成](#8-第三方服务集成)
9. [部署指南](#9-部署指南)
10. [开发规范](#10-开发规范)
11. [常见问题](#11-常见问题)

---

## 1. 项目概述

### 1.1 项目简介

途点儿啥是一个智能旅游规划协作平台，旨在解决多人旅行规划中的需求收集、路线生成、协作管理等痛点问题。

**核心价值**:
- 🤝 多人协作填写旅行需求
- 🤖 AI 智能生成旅行路线
- 📊 实时监控参与者进度
- 💰 预算管理和行程安排
- 📄 PDF 行程单导出

### 1.2 应用场景

- **团队旅游**: 公司团建、同学聚会
- **家庭出游**: 多个家庭成员共同规划
- **情侣旅行**: 双方共同参与路线设计
- **自由行规划**: 个人深度旅行定制

### 1.3 系统特色

1. **智能化**: 基于 Kimi AI 的智能路线生成
2. **协作化**: 实时协作、权限管理、状态监控
3. **可视化**: 直观的界面设计、实时数据展示
4. **移动化**: 响应式设计，支持手机端访问
5. **透明化**: 景点"xxx想去"标记，增强协作可见性
6. **灵活化**: 三级权限管理，动态权限升级
7. **实时性**: 编辑者实时修改行程，数据即时同步

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    前端层 (Frontend)                  │
│         HTML5 + CSS3 + JavaScript (原生)             │
│  login.html | index.html | create-project.html ...  │
└─────────────────┬───────────────────────────────────┘
                  │ HTTP/REST API
┌─────────────────┴───────────────────────────────────┐
│              控制层 (Controller Layer)                │
│    UserController | ProjectController | ...          │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│              业务层 (Service Layer)                   │
│  UserService | ProjectService | KimiAIService ...   │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│            数据访问层 (Repository Layer)              │
│  UserRepository | ProjectRepository | JPA/MyBatis   │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│               数据库层 (Database)                     │
│                    MySQL 8.0                         │
└─────────────────────────────────────────────────────┘
```

### 2.2 技术分层

| 层级 | 技术栈 | 职责 |
|------|--------|------|
| 前端层 | HTML/CSS/JS | 用户交互、页面渲染 |
| 控制层 | Spring MVC | 请求路由、参数验证 |
| 业务层 | Spring Boot | 业务逻辑、事务管理 |
| 数据访问层 | Spring Data JPA/MyBatis | 数据CRUD操作 |
| 数据库层 | MySQL 8.0 | 数据持久化 |

### 2.3 外部服务集成

```
┌──────────────────┐
│  Spring Boot App │
└────────┬─────────┘
         │
    ┌────┴────┬─────────┬──────────┐
    │         │         │          │
┌───┴───┐ ┌──┴───┐ ┌───┴────┐ ┌──┴────┐
│ Kimi  │ │ 高德  │ │ QQ邮箱 │ │ MySQL │
│  AI   │ │ 地图  │ │ SMTP  │ │  DB   │
└───────┘ └──────┘ └────────┘ └───────┘
```

---

## 3. 技术栈详解

### 3.1 后端技术

#### 核心框架
- **Spring Boot 2.7.0**: 应用框架
- **Spring Data JPA**: ORM 持久化框架
- **MyBatis 2.2.2**: SQL 映射框架
- **MySQL Connector 8.0.29**: 数据库驱动

#### 安全与工具
- **Spring Security Crypto**: 密码加密
- **Spring Boot Mail**: 邮件发送
- **Spring Boot Actuator**: 应用监控

#### 第三方库
- **OkHttp 4.10.0**: HTTP 客户端
- **Gson 2.9.0**: JSON 处理
- **Apache POI 5.2.3**: Office 文档处理
- **iText PDF 7.2.5**: PDF 生成
- **BouncyCastle 1.70**: 加密算法

### 3.2 前端技术

- **HTML5**: 语义化标签、表单验证
- **CSS3**: Flexbox、Grid、动画效果
- **JavaScript (ES6+)**: 原生JS，无框架依赖
- **Iconify**: 图标库
- **响应式设计**: 支持移动端和PC端

### 3.3 数据库技术

- **MySQL 8.0**: 关系型数据库
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **事务隔离级别**: READ_COMMITTED

---

## 4. 数据库设计

### 4.1 数据库表结构

#### 用户相关表

**1. users (用户表)**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    registration_time DATETIME NOT NULL,
    avatar_url VARCHAR(200),
    status VARCHAR(20) DEFAULT 'active'
);
```

#### 项目相关表

**2. travel_projects (旅行项目表)**
```sql
CREATE TABLE travel_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(100) NOT NULL,
    creator_id BIGINT NOT NULL,
    destination VARCHAR(100),
    start_date DATE,
    end_date DATE,
    days INT,
    total_budget DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'planning',
    create_time DATETIME NOT NULL,
    cover_url VARCHAR(255)
);
```

**3. project_participants (项目参与者表)**
```sql
CREATE TABLE project_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    join_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT '待填写'
);
```

**4. travel_participants (旅行参与者表)**
```sql
CREATE TABLE travel_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission VARCHAR(20) NOT NULL
);
```

#### 需求与路线表

**5. requirement_parameters (需求参数表)**
```sql
CREATE TABLE requirement_parameters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    interest_tags VARCHAR(200),
    daily_budget_allocation DECIMAL(10,2),
    wishlist TEXT,
    dislike_list TEXT,
    budget_breakdown TEXT
);
```

**6. ai_generated_routes (AI生成路线表)**
```sql
CREATE TABLE ai_generated_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    route_content TEXT,
    generation_time DATETIME NOT NULL,
    is_selected BOOLEAN DEFAULT FALSE
);
```

**7. overall_routes (总体路线表)**
```sql
CREATE TABLE overall_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    route_name VARCHAR(100),
    highlights TEXT,
    start_date DATE,
    end_date DATE,
    created_time DATETIME NOT NULL
);
```

#### 行程与预算表

**8. activity_schedules (活动行程表)**
```sql
CREATE TABLE activity_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    day_number INT,
    time_period VARCHAR(20),
    location VARCHAR(100),
    activity_description TEXT,
    duration INT,
    estimated_cost DECIMAL(10,2)
);
```

**9. budgets (预算表)**
```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(10,2),
    description TEXT,
    budget DECIMAL(10,2),
    day_number INT
);
```

#### 协作与分享表

**10. chat_messages (聊天消息表)**
```sql
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50),
    message TEXT,
    created_time DATETIME NOT NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_created_time (created_time)
);
```

**11. shared_documents (分享文档表)**
```sql
CREATE TABLE shared_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    document_url VARCHAR(200),
    format VARCHAR(20),
    generated_time DATETIME NOT NULL,
    share_link VARCHAR(200),
    creator_id BIGINT NOT NULL
);
```

### 4.2 数据库关系图

```
users (1) ─────< (N) travel_projects
  │                      │
  │                      ├─────< project_participants
  │                      ├─────< requirement_parameters
  │                      ├─────< ai_generated_routes
  │                      ├─────< activity_schedules
  │                      ├─────< budgets
  │                      ├─────< chat_messages
  │                      └─────< shared_documents
  │
  └─────< travel_participants
```

---

## 5. 核心功能模块

### 5.1 用户管理模块

**功能点**:
- 用户注册与登录
- 密码加密存储
- 用户信息管理
- 会话管理

**关键类**:
- `UserController.java` - 用户接口控制器
- `UserService.java` - 用户业务逻辑
- `UserRepository.java` - 用户数据访问
- `User.java` - 用户实体类

**API 端点**:
```
POST /api/users/register  - 用户注册
POST /api/users/login     - 用户登录
GET  /api/users/{id}      - 获取用户信息
PUT  /api/users/{id}      - 更新用户信息
```

### 5.2 项目管理模块

**功能点**:
- 创建旅行项目
- 项目列表查看
- 项目详情管理
- 项目状态更新

**关键类**:
- `TravelProjectController.java`
- `TravelProjectService.java`
- `TravelProjectRepository.java`
- `TravelProject.java`

**项目状态流转**:
```
创建 → 需求收集中 → AI生成中 → 路线选择 → 协作优化 → 完成
```

### 5.3 协作需求收集模块

**功能点**:
- 参与者邀请
- 需求表单填写
- 填写状态监控
- 权限管理（创建者/编辑者/查看者）
- 景点"xxx想去"标记显示
- 实时权限升级和UI更新

**关键类**:
- `ProjectParticipantController.java`
- `RequirementParameterController.java`
- `RequirementParameterService.java`

**协作流程**:
1. 创建者创建项目
2. 生成分享链接
3. 参与者通过链接加入
4. 填写个人需求（包括"想去的景点"）
5. 创建者实时监控进度
6. 创建者可将参与者提升为编辑者
7. 编辑者可删除景点、修改行程
8. 协作界面显示"xxx想去"标记，增强透明度

### 5.4 AI 路线生成模块

**功能点**:
- 收集所有参与者需求
- 调用 Kimi AI 生成路线
- 生成多个路线方案
- 路线选择与确认

**关键类**:
- `KimiAIService.java` - AI服务核心
- `AiGeneratedRouteController.java`
- `AiGeneratedRouteService.java`

**生成流程**:
```java
// 1. 收集需求
List<RequirementParameter> requirements = 
    requirementService.findByProjectId(projectId);

// 2. 构建 AI 提示词
String prompt = buildPrompt(project, requirements);

// 3. 调用 Kimi AI
String aiResponse = kimiAIService.generateRoute(prompt);

// 4. 保存生成结果
aiRouteService.save(projectId, aiResponse);
```

### 5.5 预算管理模块

**功能点**:
- 预算分类管理
- 费用明细记录
- 预算统计分析
- 预算提醒

**关键类**:
- `BudgetController.java`
- `BudgetService.java`
- `Budget.java`

### 5.6 行程安排模块

**功能点**:
- 每日行程规划
- 活动时间安排
- 地点信息管理
- 费用估算

**关键类**:
- `ActivityScheduleController.java`
- `ActivityScheduleService.java`
- `ActivitySchedule.java`

### 5.7 实时聊天模块

**功能点**:
- 项目内实时沟通
- 消息记录保存
- 消息推送（可扩展）

**关键类**:
- `ChatMessageController.java`
- `ChatMessageService.java`
- `ChatMessage.java`

### 5.8 文档导出模块

**功能点**:
- PDF 行程单生成
- 文档在线预览
- 分享链接生成

**关键类**:
- `ItineraryPdfService.java`
- `PdfExportController.java`
- `SharedDocumentService.java`

---

## 6. API 接口文档

### 6.1 用户管理 API

#### 用户注册
```
POST /api/users/register
Content-Type: application/json

Request Body:
{
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "password": "password123",
    "phone": "13800138000"
}

Response:
{
    "id": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "registrationTime": "2025-12-10T10:00:00"
}
```

#### 用户登录
```
POST /api/users/login
Content-Type: application/json

Request Body:
{
    "username": "zhangsan",
    "password": "password123"
}

Response:
{
    "id": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "token": "..." (可扩展)
}
```

### 6.2 项目管理 API

#### 创建项目
```
POST /api/travel-projects
Content-Type: application/json

Request Body:
{
    "projectName": "国庆七日游",
    "creatorId": 1,
    "destination": "成都",
    "startDate": "2025-10-01",
    "endDate": "2025-10-07",
    "days": 7,
    "totalBudget": 5000.00
}

Response:
{
    "id": 1,
    "projectName": "国庆七日游",
    "status": "planning",
    "createTime": "2025-12-10T10:00:00"
}
```

#### 获取项目列表
```
GET /api/travel-projects?userId=1

Response:
[
    {
        "id": 1,
        "projectName": "国庆七日游",
        "destination": "成都",
        "days": 7,
        "status": "planning"
    }
]
```

### 6.3 需求参数 API

#### 提交需求
```
POST /api/requirement-parameters
Content-Type: application/json

Request Body:
{
    "projectId": 1,
    "userId": 1,
    "interestTags": "美食,摄影,文化",
    "dailyBudgetAllocation": 500.00,
    "wishlist": "宽窄巷子,锦里",
    "dislikeList": "辛辣食物",
    "budgetBreakdown": "[{\"category\":\"交通\",\"amount\":1000}]"
}
```

### 6.4 AI 路线生成 API

#### 生成路线
```
POST /api/ai-routes/generate
Content-Type: application/json

Request Body:
{
    "projectId": 1
}

Response:
{
    "success": true,
    "routes": [
        {
            "id": 1,
            "routeContent": "...",
            "generationTime": "2025-12-10T10:30:00"
        }
    ]
}
```

### 6.5 聊天消息 API

#### 发送消息
```
POST /api/chat-messages
Content-Type: application/json

Request Body:
{
    "projectId": 1,
    "userId": 1,
    "username": "张三",
    "message": "大家觉得这条路线怎么样?"
}
```

#### 获取消息历史
```
GET /api/chat-messages/project/1

Response:
[
    {
        "id": 1,
        "username": "张三",
        "message": "大家觉得这条路线怎么样?",
        "createdTime": "2025-12-10T11:00:00"
    }
]
```

### 6.6 高德地图 API

#### 搜索POI (景点)
```
GET /api/amap-test/poi/search?city=成都&keyword=宽窄巷子

Response:
{
    "success": true,
    "pois": [
        {
            "name": "宽窄巷子",
            "address": "青羊区金河路口宽窄巷子",
            "location": "104.056,30.672",
            "type": "风景名胜"
        }
    ]
}
```

---

## 7. 前端页面说明

### 7.1 登录注册页面 (login.html)

**功能**:
- 用户登录
- 用户注册
- 表单验证

**技术实现**:
- 响应式表单设计
- 前端表单验证
- LocalStorage 存储用户信息

**关键代码**:
```javascript
// 登录处理
async function handleLogin(username, password) {
    const response = await post('/users/login', {
        username, password
    });
    
    if (response && response.id) {
        auth.setUser(response);
        window.location.href = '/index.html';
    }
}
```

### 7.2 主页 (index.html)

**功能**:
- 展示用户创建的项目
- 展示参与的项目
- 快速创建新项目

**页面布局**:
```
┌────────────────────────────────────┐
│         顶部导航栏 (Header)          │
├────────────────────────────────────┤
│         欢迎横幅 (Welcome)           │
├────────────────────────────────────┤
│     我创建的旅途 (Created)           │
│  [项目卡片1] [项目卡片2] [项目卡片3] │
├────────────────────────────────────┤
│     我参与的旅途 (Participated)      │
│  [项目卡片4] [项目卡片5]             │
└────────────────────────────────────┘
```

### 7.3 创建项目页面 (create-project.html)

**功能**:
- 填写项目基本信息
- 填写个人需求参数
- 上传项目封面
- 邀请参与者

**表单字段**:
1. 基本信息
   - 项目名称
   - 目的地
   - 出发时间
   - 返回时间
   - 总预算

2. 需求参数
   - 兴趣标签
   - 想去的景点
   - 不想去的地方
   - 每日预算分配
   - 预算明细

### 7.4 协作填写页面 (collaboration.html)

**功能**:
- 参与者填写需求
- 实时聊天
- 查看其他参与者
- 权限管理

**布局结构**:
```
┌─────────────────────────────────────────┐
│          项目标题 + 参与者头像            │
├──────────────────┬──────────────────────┤
│                  │                      │
│   需求表单区      │     实时聊天区        │
│   (左侧70%)       │     (右侧30%)        │
│                  │                      │
└──────────────────┴──────────────────────┘
```

### 7.5 参与者状态监控页面 (participants-status.html)

**功能**:
- 创建者专用页面
- 查看所有参与者填写状态
- 实时更新进度
- 催促未填写成员

**状态标识**:
- ✅ 已完成 (绿色)
- ⏳ 填写中 (黄色)
- ⏸ 待填写 (灰色)

### 7.6 路线选择页面 (route-selection.html)

**功能**:
- 展示 AI 生成的多条路线
- 路线对比
- 路线详情查看
- 选择确认

**路线卡片内容**:
- 路线名称
- 路线亮点
- 每日行程概览
- 预算预估
- 景点列表

### 7.7 分享页面 (share.html)

**功能**:
- 生成分享链接
- PDF 预览
- PDF 下载
- 社交媒体分享

---

## 8. 第三方服务集成

### 8.1 Kimi AI 集成

**配置 (application.yml)**:
```yaml
kimi:
  api:
    key: sk-xxxxxxxxxxxxxxxx
    url: https://api.moonshot.cn/v1/chat/completions
    model: moonshot-v1-8k
```

**调用示例 (KimiAIService.java)**:
```java
public String generateRoute(String prompt) {
    OkHttpClient client = new OkHttpClient();
    
    JSONObject requestBody = new JSONObject();
    requestBody.put("model", kimiModel);
    requestBody.put("messages", new JSONArray()
        .put(new JSONObject()
            .put("role", "user")
            .put("content", prompt)));
    
    Request request = new Request.Builder()
        .url(kimiApiUrl)
        .addHeader("Authorization", "Bearer " + kimiApiKey)
        .post(RequestBody.create(
            requestBody.toString(), 
            MediaType.parse("application/json")))
        .build();
    
    Response response = client.newCall(request).execute();
    return parseResponse(response.body().string());
}
```

**提示词模板**:
```
请根据以下需求生成一份详细的{days}日{destination}旅游路线：

项目信息：
- 目的地：{destination}
- 天数：{days}天
- 总预算：{budget}元

参与者需求汇总：
{participantRequirements}

请生成包含以下内容的路线：
1. 每日详细行程
2. 景点推荐及理由
3. 餐饮建议
4. 预算分配
5. 注意事项
```

### 8.2 高德地图 API 集成

**配置**:
```yaml
amap:
  api:
    key: your_amap_api_key
```

**POI 搜索 (AmapPoiService.java)**:
```java
public Map<String, Object> searchPoi(String city, String keyword) {
    String url = String.format(
        "https://restapi.amap.com/v3/place/text?key=%s&city=%s&keywords=%s",
        amapApiKey, city, keyword);
    
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();
    
    Response response = client.newCall(request).execute();
    return parsePoiResponse(response.body().string());
}
```

**功能应用**:
- 景点自动补全
- 地理编码
- 地址解析
- 距离计算

### 8.3 QQ 邮箱 SMTP 集成

**配置**:
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com
    password: your_authorization_code
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**邮件发送 (EmailService.java)**:
```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;
    
    public void sendInvitation(String to, String projectName, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("旅行项目邀请 - " + projectName);
        message.setText("您被邀请参与项目: " + projectName + 
                       "\n点击链接加入: " + link);
        
        mailSender.send(message);
    }
}
```

**应用场景**:
- 项目邀请邮件
- 状态提醒邮件
- 行程单发送

---

## 9. 部署指南

### 9.1 开发环境部署

**前置条件**:
- JDK 11+
- MySQL 8.0+
- IntelliJ IDEA
- Navicat for MySQL

**步骤**:

1. **克隆项目**
```bash
git clone https://github.com/3217741252tsl-collab/tudianersha.git
```

2. **创建数据库**
```sql
CREATE DATABASE tudianersha 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

3. **执行SQL脚本**
- 执行 `src/main/resources/schema.sql`
- 执行 `src/main/resources/data.sql`

4. **修改配置文件**
```yaml
# application.yml
spring:
  datasource:
    username: your_username
    password: your_password
```

5. **运行项目**
```bash
mvn spring-boot:run
```

或在 IDEA 中直接运行 `Application.java`

### 9.2 生产环境部署

**服务器要求**:
- Linux (推荐 Ubuntu 20.04+)
- 2核4G内存以上
- 40GB 磁盘空间

**部署步骤**:

1. **安装 JDK**
```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

2. **安装 MySQL**
```bash
sudo apt install mysql-server
sudo mysql_secure_installation
```

3. **打包应用**
```bash
mvn clean package -DskipTests
```

4. **上传 JAR 文件**
```bash
scp target/tudianersha-system-1.0-SNAPSHOT.jar user@server:/opt/app/
```

5. **启动应用**
```bash
java -jar tudianersha-system-1.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8010
```

6. **配置为系统服务 (systemd)**

创建 `/etc/systemd/system/tudianersha.service`:
```ini
[Unit]
Description=Tudianersha Travel System
After=mysql.service

[Service]
Type=simple
User=app
ExecStart=/usr/bin/java -jar /opt/app/tudianersha-system-1.0-SNAPSHOT.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl enable tudianersha
sudo systemctl start tudianersha
```

### 9.3 Docker 部署 (可选)

**Dockerfile**:
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/tudianersha-system-1.0-SNAPSHOT.jar app.jar
EXPOSE 8010
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: tudianersha
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./src/main/resources/data.sql:/docker-entrypoint-initdb.d/2-data.sql
    
  app:
    build: .
    ports:
      - "8010:8010"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tudianersha

volumes:
  mysql_data:
```

运行:
```bash
docker-compose up -d
```

---

## 10. 开发规范

### 10.1 代码规范

**命名规范**:
- 类名: PascalCase (UserService)
- 方法名: camelCase (getUserById)
- 常量名: UPPER_SNAKE_CASE (MAX_RETRY_COUNT)
- 变量名: camelCase (userId)

**注释规范**:
```java
/**
 * 根据用户ID获取用户信息
 * 
 * @param id 用户ID
 * @return 用户对象，不存在返回null
 * @throws IllegalArgumentException 如果id小于等于0
 */
public User getUserById(Long id) {
    // ...
}
```

### 10.2 Git 提交规范

**提交消息格式**:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**:
- feat: 新功能
- fix: 修复bug
- docs: 文档修改
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建工具或辅助工具变动

**示例**:
```
feat(user): 添加用户头像上传功能

- 支持 JPG/PNG 格式
- 限制大小2MB
- 自动压缩图片

Closes #123
```

### 10.3 API 设计规范

**RESTful 风格**:
```
GET    /api/users        - 获取用户列表
GET    /api/users/{id}   - 获取单个用户
POST   /api/users        - 创建用户
PUT    /api/users/{id}   - 更新用户
DELETE /api/users/{id}   - 删除用户
```

**响应格式**:
```json
{
    "success": true,
    "data": { ... },
    "message": "操作成功",
    "timestamp": "2025-12-10T10:00:00"
}
```

---

## 11. 常见问题

### Q1: 启动时数据库连接失败

**原因**: MySQL 未启动或配置错误

**解决**:
1. 检查 MySQL 服务状态
2. 验证 `application.yml` 中的配置
3. 确认数据库已创建

### Q2: AI 功能无法使用

**原因**: Kimi API Key 未配置或无效

**解决**:
1. 在 https://platform.moonshot.cn/ 获取 API Key
2. 配置到 `application.yml`
3. 检查 API 调用额度

### Q3: 邮件发送失败

**原因**: SMTP 配置错误或授权码无效

**解决**:
1. 登录 QQ 邮箱获取授权码
2. 确认 SMTP 端口587可用
3. 检查防火墙设置

### Q4: 前端无法访问后端 API

**原因**: 端口不一致或 CORS 配置问题

**解决**:
1. 确认后端运行在 8010 端口
2. 检查 `WebConfig.java` 中的 CORS 配置
3. 使用浏览器开发者工具查看网络请求

---

## 附录 A: 项目文件清单

### 后端 Java 文件

**配置类** (2个):
- DatabaseConfig.java
- WebConfig.java

**控制器** (18个):
- UserController.java
- TravelProjectController.java
- ProjectParticipantController.java
- RequirementParameterController.java
- AiGeneratedRouteController.java
- OverallRouteController.java
- ActivityScheduleController.java
- BudgetController.java
- ChatMessageController.java
- TravelParticipantController.java
- TravelSessionController.java
- SharedDocumentController.java
- EmailController.java
- PdfExportController.java
- AmapTestController.java
- ApiTestController.java
- HealthController.java
- MainController.java

**实体类** (12个):
- User.java
- TravelProject.java
- ProjectParticipant.java
- RequirementParameter.java
- AiGeneratedRoute.java
- OverallRoute.java
- ActivitySchedule.java
- Budget.java
- ChatMessage.java
- TravelParticipant.java
- TravelSession.java
- SharedDocument.java

**Repository** (12个):
- 对应每个实体的 Repository 接口

**Service** (15个):
- UserService.java
- TravelProjectService.java
- ProjectParticipantService.java
- RequirementParameterService.java
- AiGeneratedRouteService.java
- OverallRouteService.java
- ActivityScheduleService.java
- BudgetService.java
- ChatMessageService.java
- TravelParticipantService.java
- TravelSessionService.java
- SharedDocumentService.java
- KimiAIService.java
- AmapPoiService.java
- EmailService.java
- ItineraryPdfService.java

### 前端文件

**HTML 页面** (7个):
- login.html - 登录注册
- index.html - 主页
- create-project.html - 创建项目
- collaboration.html - 协作填写
- participants-status.html - 参与者状态
- route-selection.html - 路线选择
- share.html - 分享页面

**CSS 文件**:
- common.css - 公共样式

**JavaScript 文件**:
- common.js - 公共函数

### 配置文件

- application.yml - 应用配置
- pom.xml - Maven 配置
- schema.sql - 数据库表结构
- data.sql - 初始数据
- .gitignore - Git 忽略文件

---

## 附录 B: 技术支持

**GitHub 仓库**: https://github.com/3217741252tsl-collab/tudianersha

**问题反馈**: 
- GitHub Issues
- Email: 3217741252tsl@gmail.com

**文档更新日期**: 2025-12-10

---

## 附录 C: 更新日志

### v1.1.0 (2025-12-10)

**新增功能**:
- ✨ 协作界面景点"xxx想去"标记功能
  - 加载所有用户需求参数
  - 模糊匹配景点名称算法
  - 景点列表和路线总览显示用户名
  - 增强协作可见性和透明度

**功能优化**:
- 🔧 权限管理优化
  - 主页项目分类使用`projectParticipant.role`判断
  - 编辑者权限项目正确显示在"我编辑的项目"板块
  - 权限升级后自动重新检查并更新UI
  - 统一使用中文`role`字段（"创建者"/"编辑者"/"查看者"）

- 🔧 数据持久化优化
  - `AiGeneratedRouteController.updateAiGeneratedRoute()`添加所有字段更新
  - `AiGeneratedRouteService.saveAiGeneratedRoute()`添加`@Transactional`注解
  - 确保删除景点操作正确保存到数据库

**Bug 修复**:
- 🐛 修复编辑者删除景点后数据不持久化
  - PUT接口缺少`dailyItinerary`字段更新
  - Service层缺少事务注解
  - 前端变量作用域冲突（`const dailyItinerary` vs 全局变量）

- 🐛 修复权限检查字段错误
  - 协作界面检查`role === '编辑者'`而非`permission === 'EDIT'`
  - 主页项目分类检查`projectParticipant.role`而非`participant.permission`

- 🐛 修复变量命名冲突
  - `deleteAttraction()`和`saveActivity()`中局部变量改名为`localItinerary`
  - 避免与全局变量`dailyItinerary`冲突

**技术改进**:
- 前端：`collaboration.html` 添加需求参数加载和景点匹配逻辑
- 前端：`index.html` 修正项目分类逻辑
- 后端：`AiGeneratedRouteController` 完善PUT接口字段更新
- 后端：`AiGeneratedRouteService` 添加事务支持

### v1.0.0 (2025-12-09)

**初始版本功能**:
- ✅ 用户注册登录系统
- ✅ 旅行项目创建和管理
- ✅ 多人协作需求收集
- ✅ AI智能路线生成（Kimi AI）
- ✅ 高德地图POI搜索
- ✅ 预算管理
- ✅ 行程安排
- ✅ 实时聊天
- ✅ PDF行程单导出
- ✅ 邮件通知

---

**© 2025 途点儿啥旅游规划系统 | MIT License**
