# 途点儿啥旅游规划系统 (Tudianersha Travel Planning System)

一个基于 Spring Boot + MySQL 的智能旅游规划协作系统，支持多人协作需求收集、AI 智能路线生成、预算管理等功能。

## 📋 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [配置说明](#配置说明)
- [功能页面](#功能页面)
- [常见问题](#常见问题)
- [开发指南](#开发指南)

---

## 项目简介

途点儿啥是一个智能旅游规划协作平台，帮助用户：
- 创建旅行项目并邀请参与者协作
- 收集所有参与者的旅行需求（目的地、时间、预算、景点偏好等）
- 基于收集的需求，通过 AI 生成智能旅行路线
- 进行预算管理和行程安排
- 导出行程 PDF 文档

## 核心功能

- ✅ **用户管理**：注册、登录、个人信息管理
- ✅ **项目管理**：创建旅行项目、邀请参与者
- ✅ **协作需求收集**：参与者填写旅行需求表单
- ✅ **创建者监控**：实时查看所有参与者的填写状态
- ✅ **AI 路线生成**：集成 Kimi AI，根据需求智能生成旅行路线
- ✅ **高德地图集成**：景点搜索和地理信息服务
- ✅ **预算管理**：记录和管理旅行预算
- ✅ **行程安排**：详细的日程规划
- ✅ **实时聊天**：参与者之间的实时交流
- ✅ **文档导出**：生成 PDF 行程单
- ✅ **邮件通知**：项目邀请和状态通知

## 技术栈

### 后端
- **Java 11**
- **Spring Boot 2.7.0**
- **Spring Data JPA** - 数据持久化
- **MyBatis** - SQL 映射
- **MySQL 8.0** - 数据库
- **Spring Security** - 密码加密
- **Spring Mail** - 邮件服务

### 前端
- **HTML5 + CSS3**
- **JavaScript (原生)**
- **响应式设计**

### 第三方服务
- **Kimi AI API** - 智能路线生成
- **高德地图 API** - 地理位置服务
- **QQ 邮箱 SMTP** - 邮件发送

### 工具库
- **Apache POI** - Office 文档处理
- **iText PDF** - PDF 生成
- **OkHttp** - HTTP 客户端
- **Gson** - JSON 处理

## 环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 11+ | Java 开发环境 |
| MySQL | 8.0+ | 数据库 |
| Maven | 3.6+ | 构建工具（IDEA 自带）|
| IntelliJ IDEA | 2020+ | 开发工具 |
| Navicat for MySQL | 任意版本 | 数据库管理工具（推荐）|

### 可选配置
- Kimi AI API Key（用于 AI 路线生成功能）
- 高德地图 API Key（用于景点搜索功能）
- QQ 邮箱授权码（用于邮件发送功能）

---

## 快速开始

### 步骤 1：克隆项目

1. 打开 IntelliJ IDEA
2. 选择 `File` → `New` → `Project from Version Control`
3. 输入 Git 仓库地址：
   ```
   https://github.com/3217741252tsl-collab/tudianersha.git
   ```
4. 选择保存位置，点击 `Clone`

### 步骤 2：配置数据库

#### 使用 Navicat 创建数据库

1. 打开 **Navicat for MySQL**
2. 新建连接：
   - 连接名：`tudianersha`
   - 主机：`localhost`
   - 端口：`3306`
   - 用户名：`root`
   - 密码：你的 MySQL 密码
3. 测试连接成功后，右键连接 → `新建数据库`
   - 数据库名：`tudianersha`
   - 字符集：`utf8mb4`
   - 排序规则：`utf8mb4_unicode_ci`

#### 导入数据库脚本

1. 打开 `tudianersha` 数据库
2. 新建查询窗口，依次执行：
   - `src/main/resources/schema.sql` - 创建表结构
   - `src/main/resources/data.sql` - 导入初始数据

### 步骤 3：修改配置文件

打开 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tudianersha?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root          # 修改为你的 MySQL 用户名
    password: 123456        # 修改为你的 MySQL 密码
```

### 步骤 4：安装 Maven 依赖

1. IDEA 会自动识别 Maven 项目
2. 等待右下角依赖下载完成（首次需要 5-10 分钟）
3. 如未自动导入，点击右侧 `Maven` 面板 → 刷新按钮

### 步骤 5：运行项目

#### 方法一：运行主类（推荐）

1. 找到 `src/main/java/com/tudianersha/Application.java`
2. 右键点击 → `Run 'Application'`

#### 方法二：Maven 命令运行

1. 打开右侧 `Maven` 面板
2. 展开 `Plugins` → `spring-boot` → 双击 `spring-boot:run`

### 步骤 6：验证运行

1. 控制台显示：`Started Application in X.XXX seconds`
2. 浏览器访问：http://localhost:8010
3. 健康检查：http://localhost:8010/api/health（返回 "OK"）

---

## 项目结构

```
tudianersha/
├── src/main/java/com/tudianersha/
│   ├── Application.java                 # 主启动类
│   ├── config/                          # 配置类
│   │   ├── DatabaseConfig.java          # 数据库配置
│   │   └── WebConfig.java              # Web 配置（CORS）
│   ├── controller/                      # 控制器层
│   │   ├── UserController.java          # 用户管理
│   │   ├── TravelProjectController.java # 项目管理
│   │   ├── AiGeneratedRouteController.java # AI 路线
│   │   ├── BudgetController.java        # 预算管理
│   │   ├── ChatMessageController.java   # 聊天功能
│   │   └── ...                         # 其他控制器
│   ├── entity/                          # 实体类
│   │   ├── User.java                    # 用户实体
│   │   ├── TravelProject.java           # 项目实体
│   │   └── ...                         # 其他实体
│   ├── repository/                      # 数据访问层
│   ├── service/                         # 业务逻辑层
│   │   ├── KimiAIService.java          # Kimi AI 服务
│   │   ├── AmapPoiService.java         # 高德地图服务
│   │   ├── EmailService.java           # 邮件服务
│   │   └── ...                         # 其他服务
│   └── util/                            # 工具类
├── src/main/resources/
│   ├── static/                          # 静态资源
│   │   ├── css/common.css              # 样式文件
│   │   ├── js/common.js                # 公共 JS
│   │   ├── login.html                  # 登录页面
│   │   ├── index.html                  # 主页
│   │   ├── create-project.html         # 创建项目
│   │   ├── collaboration.html          # 协作填写
│   │   ├── participants-status.html    # 参与者状态
│   │   ├── route-selection.html        # 路线选择
│   │   └── share.html                  # 分享页面
│   ├── application.yml                  # 应用配置
│   ├── schema.sql                       # 数据库表结构
│   └── data.sql                         # 初始数据
├── pom.xml                              # Maven 配置
└── README.md                            # 项目说明
```

---

## 配置说明

### 基础配置 (application.yml)

```yaml
# 服务端口
server:
  port: 8010

# 数据库配置（必需）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tudianersha?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

# JPA 配置
  jpa:
    hibernate:
      ddl-auto: update        # 自动更新表结构
    show-sql: true           # 显示 SQL 语句
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

### 可选配置

#### 1. 邮件服务配置（可选）

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com      # 改为你的 QQ 邮箱
    password: your_authorization_code # 改为你的授权码
```

**获取 QQ 邮箱授权码：**
- 登录 QQ 邮箱 → 设置 → 账户 → POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务
- 开启 SMTP 服务 → 生成授权码

#### 2. Kimi AI 配置（可选）

```yaml
kimi:
  api:
    key: sk-xxxxxxxxxxxxxxxx    # 替换为你的 Kimi API Key
    url: https://api.moonshot.cn/v1/chat/completions
    model: moonshot-v1-8k
```

**获取 Kimi API Key：**
- 访问 https://platform.moonshot.cn/
- 注册并创建 API Key

#### 3. 高德地图配置（可选）

```yaml
amap:
  api:
    key: xxxxxxxxxxxxxxxx       # 替换为你的高德地图 API Key
```

**获取高德地图 API Key：**
- 访问 https://console.amap.com/
- 注册并创建应用，获取 Web 服务 API Key

---

## 功能页面

| 功能 | 访问路径 | 说明 |
|------|---------|------|
| 登录/注册 | http://localhost:8010/login.html | 用户登录和注册 |
| 系统主页 | http://localhost:8010/index.html | 项目列表和管理 |
| 创建项目 | http://localhost:8010/create-project.html | 创建新的旅行项目 |
| 协作填写 | http://localhost:8010/collaboration.html | 参与者填写需求 |
| 参与者状态 | http://localhost:8010/participants-status.html | 创建者查看填写进度 |
| 路线选择 | http://localhost:8010/route-selection.html | 查看和选择 AI 生成的路线 |
| 分享页面 | http://localhost:8010/share.html | 项目分享和邀请 |
| 健康检查 | http://localhost:8010/api/health | 服务健康状态检查 |

---

## 常见问题

### Q1: 端口 8010 被占用怎么办？

**解决方法：** 修改 `application.yml` 中的端口号

```yaml
server:
  port: 8011  # 改为其他可用端口
```

### Q2: 数据库连接失败

**可能原因：**
- MySQL 服务未启动
- 用户名或密码错误
- 数据库不存在

**解决方法：**
1. 在 Navicat 中测试连接
2. 确认 `application.yml` 中的配置正确
3. 确认数据库 `tudianersha` 已创建

### Q3: Maven 依赖下载失败

**解决方法：** 配置国内镜像

1. `File` → `Settings` → `Build Tools` → `Maven`
2. 配置阿里云镜像（在 settings.xml 中添加）

### Q4: 缺少 JDK 或版本不对

**解决方法：**
1. `File` → `Project Structure` → `Project`
2. 设置 `SDK` 为 JDK 11+
3. 设置 `Language level` 为 11

### Q5: AI 功能无法使用

**原因：** 未配置 Kimi API Key

**解决方法：**
- 在 `application.yml` 中配置有效的 Kimi API Key
- 或暂时不使用 AI 功能，手动创建路线

---

## 开发指南

### 本地开发

1. **启动项目**
   ```bash
   mvn spring-boot:run
   ```

2. **编译打包**
   ```bash
   mvn clean package
   ```

3. **运行测试**
   ```bash
   mvn test
   ```

### 添加新功能

1. 在 `entity/` 创建实体类
2. 在 `repository/` 创建数据访问接口
3. 在 `service/` 实现业务逻辑
4. 在 `controller/` 创建 REST API
5. 在 `static/` 创建前端页面

### 数据库修改

1. 修改实体类
2. 运行项目（JPA 会自动更新表结构）
3. 或手动编写 SQL 迁移脚本

### 推荐开发工具

- **IntelliJ IDEA** - Java 开发
- **Navicat for MySQL** - 数据库管理
- **Postman** - API 测试
- **Chrome DevTools** - 前端调试

---

## API 接口示例

### 用户管理

```
POST   /api/users/register          # 用户注册
POST   /api/users/login             # 用户登录
GET    /api/users/{id}              # 获取用户信息
PUT    /api/users/{id}              # 更新用户信息
```

### 项目管理

```
GET    /api/travel-projects         # 获取项目列表
POST   /api/travel-projects         # 创建项目
GET    /api/travel-projects/{id}    # 获取项目详情
PUT    /api/travel-projects/{id}    # 更新项目
DELETE /api/travel-projects/{id}    # 删除项目
```

### AI 路线生成

```
POST   /api/ai-routes/generate      # 生成 AI 路线
GET    /api/ai-routes/{projectId}   # 获取项目路线
```

更多 API 接口请查看各个 Controller 类。

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

---

## 许可证

MIT License

---

## 联系方式

- GitHub: https://github.com/3217741252tsl-collab/tudianersha
- Email: 3217741252tsl@gmail.com

---

**祝你使用愉快！** 🎉