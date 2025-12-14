# 途点儿啥后端启动指南

**创建时间**: 2025年12月13日  
**适用环境**: Windows + IntelliJ IDEA

---

## ⚠️ 发现的配置问题

### 问题1: 端口配置冲突 ❌

**当前配置**：
```yaml
# application.yml
server:
  port: 8080  # ← 当前配置
```

**问题说明**：
- 您的记忆显示项目应该运行在 **8010** 端口
- 但 `application.yml` 中配置的是 **8080** 端口
- 前端页面可能配置的API地址是 `http://localhost:8010`

**影响**：
- ❌ 后端启动在8080，前端请求8010 → API调用失败
- ❌ 可能导致跨域问题
- ❌ 前后端无法通信

---

## ✅ 启动前检查清单

### 1. 数据库检查

**检查MySQL是否运行**：

```powershell
# 打开PowerShell，执行
mysql -u root -p
# 输入密码: 123456
```

**预期结果**：
```
Welcome to the MySQL monitor...
mysql>
```

**如果失败**：
```powershell
# 启动MySQL服务
net start MySQL80
```

**数据库验证**：
```sql
-- 登录MySQL后执行
SHOW DATABASES;
-- 应该看到 tudianersha 数据库

USE tudianersha;
SHOW TABLES;
-- 应该看到所有数据表
```

---

### 2. 端口检查

**检查8010端口是否被占用**：

```powershell
# PowerShell执行
netstat -ano | findstr "8010"
```

**预期结果**：
- 没有输出 → 端口未被占用 ✅
- 有输出 → 端口被占用 ❌

**如果端口被占用**：
```powershell
# 查看占用进程的PID（假设是1234）
taskkill /PID 1234 /F
```

---

### 3. Java环境检查

**检查Java版本**：

```powershell
java -version
```

**预期结果**：
```
java version "11.0.x"
Java(TM) SE Runtime Environment
```

**版本要求**：
- ✅ Java 11 或更高版本
- ❌ Java 8 不兼容（项目使用Java 11特性）

---

## 🔧 配置修复

### 修复端口配置

**需要修改的文件**：`tudianersha/src/main/resources/application.yml`

**修改内容**：

```yaml
server:
  port: 8010  # 修改为8010
```

**完整修改后的配置**：

```yaml
server:
  port: 8010

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tudianersha?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.MySQL8Dialect

  sql:
    init:
      mode: never

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.tudianersha.entity

logging:
  level:
    com.tudianersha: debug

# Kimi AI Configuration
kimi:
  api:
    key: sk-bCclui8VTON2LeiUgmNSUwPiC6FxmyIwpkXvidii4m4NQoTI
    url: https://api.moonshot.cn/v1/chat/completions
    model: moonshot-v1-8k
```

---

## 🚀 IDEA启动步骤

### 方法1: 运行主类（推荐）

**步骤**：

1. **打开项目**
   - 启动 IntelliJ IDEA
   - 打开项目: `e:/tudianershatest/tudianersha`

2. **定位主类**
   - 在项目树中找到: `src/main/java/com/tudianersha/Application.java`

3. **运行主类**
   - 右键点击 `Application.java`
   - 选择 "Run 'Application.main()'"
   - 或点击类名左侧的绿色三角形 ▶️

**启动截图制作**：
- 启动后，截取IDEA底部的"Run"窗口
- 应该看到 Spring Boot 的启动日志

---

### 方法2: Maven命令启动

**步骤**：

1. **打开IDEA终端**
   - 点击IDEA底部的 "Terminal" 标签
   - 或按快捷键 `Alt + F12`

2. **切换到项目目录**
   ```powershell
   cd tudianersha
   ```

3. **执行Maven命令**
   ```powershell
   mvn spring-boot:run
   ```

---

## ✅ 启动成功标志

### 控制台输出应包含

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.0)

[INFO] Starting Application using Java 11.0.x
[INFO] No active profile set, falling back to default profiles: default
[INFO] Tomcat initialized with port(s): 8010 (http)  ← 确认端口8010
[INFO] Starting service [Tomcat]
[INFO] Starting Servlet engine: [Apache Tomcat/9.0.x]
[INFO] HikariPool-1 - Starting...  ← 数据库连接池启动
[INFO] HikariPool-1 - Start completed.
[INFO] Tomcat started on port(s): 8010 (http) with context path ''
[INFO] Started Application in 5.234 seconds
```

### 关键检查点

| 检查项 | 预期结果 | 说明 |
|--------|---------|------|
| **端口** | `Tomcat started on port(s): 8010` | 确认运行在8010端口 |
| **数据库连接** | `HikariPool-1 - Start completed` | 数据库连接成功 |
| **启动完成** | `Started Application in X.XXX seconds` | 应用启动完成 |
| **无ERROR日志** | 没有红色的ERROR字样 | 无启动错误 |

---

## 🔍 启动验证

### 1. 浏览器验证

**打开浏览器，访问**：

```
http://localhost:8010/login.html
```

**预期结果**：
- ✅ 显示登录页面
- ❌ 无法访问 → 检查端口配置

### 2. API验证

**打开浏览器，访问健康检查接口**：

```
http://localhost:8010/api/health
```

**预期结果**：
```json
{
  "status": "UP"
}
```

### 3. 控制台验证

**在IDEA的Run窗口中**：

- ✅ 没有红色ERROR日志
- ✅ 看到 "Started Application" 字样
- ✅ 端口显示为8010

---

## ❌ 常见启动错误及解决方案

### 错误1: 端口已被占用

**错误信息**：
```
***************************
APPLICATION FAILED TO START
***************************

Description:

Web server failed to start. Port 8010 was already in use.

Action:

Identify and stop the process that's listening on port 8010 or configure this application to listen on another port.
```

**解决方案**：
```powershell
# 1. 查找占用进程
netstat -ano | findstr "8010"

# 2. 终止进程（假设PID是1234）
taskkill /PID 1234 /F

# 3. 重新启动应用
```

---

### 错误2: 数据库连接失败

**错误信息**：
```
Error creating bean with name 'dataSource'
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago.
```

**原因**：
- MySQL服务未启动
- 数据库不存在
- 用户名/密码错误

**解决方案**：
```powershell
# 1. 启动MySQL服务
net start MySQL80

# 2. 验证数据库
mysql -u root -p
# 输入密码: 123456

# 3. 检查数据库
SHOW DATABASES;
# 应该看到 tudianersha

# 4. 如果没有，创建数据库
CREATE DATABASE tudianersha DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### 错误3: 找不到主类

**错误信息**：
```
Error: Could not find or load main class com.tudianersha.Application
```

**解决方案**：
1. 点击IDEA菜单: `Build` → `Rebuild Project`
2. 等待重新编译完成
3. 重新运行主类

---

### 错误4: Maven依赖下载失败

**错误信息**：
```
Failed to execute goal on project tudianersha-system: 
Could not resolve dependencies
```

**解决方案**：

```powershell
# 1. 清理Maven缓存
mvn clean

# 2. 重新下载依赖
mvn install -U

# 3. 如果还是失败，删除本地仓库
# 删除 C:\Users\你的用户名\.m2\repository
# 然后重新执行 mvn install
```

---

## 📸 截图指导

### 启动成功截图

**截图1: IDEA Run窗口**

**制作方式**：
1. 启动应用后，点击IDEA底部的"Run"标签
2. 截取完整的日志输出窗口
3. 确保包含 "Started Application" 字样

**应该包含的内容**：
```
[INFO] Tomcat started on port(s): 8010 (http)
[INFO] Started Application in X.XXX seconds
```

---

**截图2: 浏览器访问**

**制作方式**：
1. 浏览器打开 `http://localhost:8010/login.html`
2. 截取完整的登录页面
3. 确保地址栏显示正确的URL

---

**截图3: API健康检查**

**制作方式**：
1. 浏览器打开 `http://localhost:8010/api/health`
2. 截取返回的JSON结果
3. 应该显示 `{"status":"UP"}`

---

## 🎯 快速检查脚本

**创建检查脚本**：

将以下内容保存为 `check-backend.ps1`：

```powershell
# 途点儿啥后端启动检查脚本

Write-Host "=== 途点儿啥后端启动检查 ===" -ForegroundColor Green

# 1. 检查Java
Write-Host "`n[1] 检查Java版本..." -ForegroundColor Yellow
java -version

# 2. 检查MySQL
Write-Host "`n[2] 检查MySQL服务..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name MySQL80 -ErrorAction SilentlyContinue
if ($mysqlService) {
    Write-Host "MySQL服务状态: $($mysqlService.Status)" -ForegroundColor Cyan
} else {
    Write-Host "MySQL服务未安装" -ForegroundColor Red
}

# 3. 检查8010端口
Write-Host "`n[3] 检查8010端口..." -ForegroundColor Yellow
$port = netstat -ano | findstr "8010"
if ($port) {
    Write-Host "端口8010已被占用:" -ForegroundColor Red
    Write-Host $port
} else {
    Write-Host "端口8010空闲" -ForegroundColor Green
}

# 4. 检查数据库连接
Write-Host "`n[4] 检查数据库..." -ForegroundColor Yellow
try {
    $result = mysql -u root -p123456 -e "SHOW DATABASES LIKE 'tudianersha';" 2>&1
    if ($result -match "tudianersha") {
        Write-Host "数据库 tudianersha 存在" -ForegroundColor Green
    } else {
        Write-Host "数据库 tudianersha 不存在" -ForegroundColor Red
    }
} catch {
    Write-Host "无法连接MySQL" -ForegroundColor Red
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Green
```

**使用方式**：
```powershell
# 在PowerShell中执行
.\check-backend.ps1
```

---

## 📝 启动日志示例

**正常启动日志**：

```log
2025-12-13 14:30:00.123  INFO 12345 --- [main] com.tudianersha.Application : Starting Application using Java 11.0.16
2025-12-13 14:30:00.456  INFO 12345 --- [main] com.tudianersha.Application : No active profile set, falling back to default
2025-12-13 14:30:01.234  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8010 (http)
2025-12-13 14:30:02.345  INFO 12345 --- [main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-12-13 14:30:02.567  INFO 12345 --- [main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-12-13 14:30:03.456  INFO 12345 --- [main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo
2025-12-13 14:30:04.234  INFO 12345 --- [main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 5.6.9.Final
2025-12-13 14:30:05.123  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8010 (http)
2025-12-13 14:30:05.234  INFO 12345 --- [main] com.tudianersha.Application              : Started Application in 5.234 seconds
```

**关键时间节点**：
- 00:00 - 应用开始启动
- 01:00 - Tomcat初始化
- 02:00 - 数据库连接池启动
- 03:00 - JPA初始化
- 05:00 - 启动完成

**总耗时**: 约5-10秒

---

## ⏱️ 下一步操作

启动成功后，您可以：

1. ✅ 访问登录页面测试: `http://localhost:8010/login.html`
2. ✅ 测试用户注册功能
3. ✅ 测试项目创建功能
4. ✅ 测试AI路线生成功能

---

**文档创建**: AI Assistant  
**最后更新**: 2025年12月13日  
**适用版本**: v1.2
